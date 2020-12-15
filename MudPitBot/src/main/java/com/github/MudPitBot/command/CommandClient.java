package com.github.MudPitBot.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;

import com.github.MudPitBot.command.exceptions.*;
import com.github.MudPitBot.sound.LavaPlayerAudioProvider;
import com.github.MudPitBot.sound.TrackScheduler;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import discord4j.voice.VoiceConnection.State;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.Logger;
import reactor.util.Loggers;

/**
 * A client class for the Command design pattern. A client is an object that
 * controls the command execution process by specifying what commands to execute
 * and at what stages of the process to execute them.
 * https://www.baeldung.com/java-command-pattern
 */
public class CommandClient {

	private static final Logger LOGGER = Loggers.getLogger(CommandClient.class);
	private GatewayDiscordClient client;
	private CommandExecutor executor = new CommandExecutor();
	private static CommandClient instance;

	// Singleton create method
	public static CommandClient create(GatewayDiscordClient client) {
		if (instance == null)
			instance = new CommandClient(client);

		return instance;
	}

	private CommandClient(GatewayDiscordClient client) {
		this.client = client;

		setupListener();
		LOGGER.info(("Command Client created."));
	}

	/*
	 * Sets up a listener on the event dispatcher.
	 */
	private void setupListener() {
		/*
		 * Add listener for new messages being sent. Whenever a messaged is typed in
		 * chat that the bot is able to see it should filter through this method.
		 */
		if (client.getEventDispatcher() != null) {

			// process ReadyEvent and reconnect to any voice channels the bot is still in
			client.getEventDispatcher().on(ReadyEvent.class).subscribe(event -> {
				processReadyEvent(event);
			});

			client.getEventDispatcher().on(MessageCreateEvent.class)
					// ignore any messages sent from a bot
					.filter(event -> !event.getMessage().getAuthor().map(User::isBot).orElse(true))
					// subscribe is like block, in that it will *request* for action
					// to be done, but instead of blocking the thread, waiting for it
					// to finish, it will just execute the results asynchronously.
					.subscribe(event -> {
						// 3.1 Message.getContent() is a String
						final String content = event.getMessage().getContent();

						// process new message to check for commands
						// subscribe to any commands to process
						processMessage(event, content);
					});
		}

	}

	/**
	 * Processes the message to check for commands and execute those commands
	 * 
	 * @param event   event of the message
	 * @param content content of the message
	 */
	public void processMessage(MessageCreateEvent event, String content) {
		// split content at ! to allow for compound commands (more
		// than 1 command in 1 message)
		// this regex splits at !, but doesn't remove it from the resulting string
		String[] commands = content.split("(?=" + Commands.COMMAND_PREFIX + ")");
		ArrayList<Mono<Void>> commandList = new ArrayList<Mono<Void>>();
		for (String command : commands) {
			command = command.trim();
			for (final Entry<String, Command> entry : Commands.getEntries()) {
				// We will be using ! as our "prefix" to any command in the system.
				String[] splitCommand = command.split(" ");

				if (splitCommand[0].toLowerCase().startsWith(Commands.COMMAND_PREFIX + entry.getKey().toLowerCase())) {
					// LOGGER.info("Processing command " + splitCommand[0]);

					// matching command found, so process and execute that command
					// copy removes the command itself from the parameters
					String[] commandParams = Arrays.copyOfRange(splitCommand, 1, splitCommand.length);
					// logs the time taken to execute the command
					commandList.add(processCommand(event, entry.getValue(), commandParams)
							.defaultIfEmpty(CommandResponse.emptyResponse()).elapsed()
							.doOnNext(TupleUtils.consumer((elapsed, response) -> LOGGER
									.info("{} took {} ms to be processed", splitCommand[0], elapsed)))
							.then());

					// we found a matching command so stop the loop
					break;
				}
			}
		}
		// create a new flux of the list of commands obtained from the message
		Flux.fromIterable(commandList).subscribe(mono -> {
			mono.onErrorResume(CommandException.class, error -> {
				LOGGER.error(error.getMessage());
				// SendMessagesException automatically get sent as private messages to the user
				// who used
				// the commands
				if (error instanceof SendMessagesException)
					sendReply(event, CommandResponse.createFlat(error.getMessage())).subscribe();

				return Mono.empty();
			}).doOnError(error -> LOGGER.error(error.getMessage(), error)).subscribe();
		});
	}

	private Mono<CommandResponse> processCommand(MessageCreateEvent event, Command command, String[] params) {

		// commands will return any string that the bot should send back as a message to
		// the command
		// CommandResponse response = executor.executeCommand(command, event, params);
		return executor.executeCommand(command, event, params).flatMap(response -> {
			if (response.getSpec() == null)
				return CommandResponse.empty();

			return sendReply(event, response);
		});
	}

	/**
	 * Called once initial handshakes with gateway are completed. Will reconnect any
	 * voice channels the bot is still connected to and create a TrackScheduler
	 * 
	 * @param event the {@link ReadyEvent}
	 */
	private void processReadyEvent(ReadyEvent event) {
		Flux.fromIterable(event.getGuilds()).subscribe(guild -> {
			event.getSelf().asMember(guild.getId()).flatMap(Member::getVoiceState).flatMap(VoiceState::getChannel)
					.flatMap(channel -> {
						TrackScheduler scheduler = new TrackScheduler(channel.getId().asLong());
						return channel
								.join(spec -> spec.setProvider(new LavaPlayerAudioProvider(scheduler.getPlayer())))
								.doOnNext(vc -> {
									// subscribe to connected/disconnected events
									vc.onConnectOrDisconnect().subscribe(newState -> {
										if (newState.equals(State.CONNECTED)) {
											LOGGER.info("Bot connected to channel with id " + channel.getId().asLong());
										} else if (newState.equals(State.DISCONNECTED)) {
											// remove the scheduler from the map.
											// This doesn't ever seem to happen when the bot
											// disconnects, though, so also remove it from map
											// during leave command
											TrackScheduler.removeFromMap(channel.getId().asLong());
											LOGGER.info("Bot disconnected from channel with id "
													+ channel.getId().asLong());
										}
									});
								});
					}).elapsed()
					.doOnNext(TupleUtils.consumer(
							(elapsed, response) -> LOGGER.info("ReadyEvent took {} ms to be processed", elapsed)))
					.subscribe();
		});
	}

	// TODO: Move this to a util class?

	public static Mono<CommandResponse> sendReply(MessageCreateEvent event, CommandResponse response) {
		// send reply, on CommandException error send the message to the member in a
		// private message
		return sendReply(event.getMessage().getChannel(), response).onErrorResume(CommandException.class, error -> {
			return Mono.justOrEmpty(event.getMember()).flatMap(member -> {
				//
				return sendPrivateReply(member.getPrivateChannel(), response);
			});
		});
	}

	public static Mono<CommandResponse> sendReply(Mono<MessageChannel> channelMono, CommandResponse response) {
		return channelMono.flatMap(channel -> {
			Mono<PermissionSet> permissions = Command.requireBotPermissions((GuildChannel) channel,
					Permission.SEND_MESSAGES);
			if (channel instanceof PrivateChannel)
				permissions = Mono.just(PermissionSet.all());

			return permissions.flatMap(ignored -> {
				if (response.getSpec() != null) {
					return channel.createMessage(response.getSpec()).flatMap(message -> {
						// if the response contained a poll
						if (response.getPoll() != null) {
							// add reactions as vote tickers, number of reactions depends on number of
							// answers
							response.getPoll().addReactions(message);
						}
						return Mono.just(response);
					});
				}
				return Mono.empty();
			});
		});
	}

	private static Mono<CommandResponse> sendPrivateReply(Mono<PrivateChannel> privateChannelMono,
			CommandResponse response) {
		return privateChannelMono.flatMap(privateChannel -> {
			return privateChannel.createMessage(response.getSpec()).thenReturn(response);
		});
	}
}
