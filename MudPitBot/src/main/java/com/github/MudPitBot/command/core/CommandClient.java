package com.github.MudPitBot.command.core;

import java.util.Arrays;
import java.util.Map.Entry;
import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandException;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.Commands;
import com.github.MudPitBot.sound.LavaPlayerAudioProvider;
import com.github.MudPitBot.sound.TrackScheduler;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
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
						processMessage(event, content).doOnError(error -> {
							LOGGER.error(error.getMessage());
							// if the error is a CommandException we should send back the error message to
							// the user
							if (error instanceof CommandException) {
								sendReply(event, CommandResponse.createFlat(error.getMessage())).subscribe();
							}
						}).subscribe();
					});
		}

	}

	/**
	 * Processes the message to check for commands and execute those commands
	 * 
	 * @param event   event of the message
	 * @param content content of the message
	 * @return
	 */
	public Mono<Void> processMessage(MessageCreateEvent event, String content) {
		Mono<Void> mono = Mono.empty();
		// split content at ! to allow for compound commands (more
		// than 1 command in 1 message)
		// this regex splits at !, but doesn't remove it from the resulting string
		String[] commands = content.split("(?=" + Commands.COMMAND_PREFIX + ")");
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
					mono = mono.then(processCommand(event, entry.getValue(), commandParams)
							.defaultIfEmpty(CommandResponse.emptyResponse()).elapsed()
							.doOnNext(TupleUtils.consumer((elapsed, response) -> LOGGER
									.info("{} took {} ms to be processed", splitCommand[0], elapsed)))
							.then());

					// we found a matching command so stop the loop
					break;
				}
			}
		}

		return mono;
	}

	private Mono<CommandResponse> processCommand(MessageCreateEvent event, Command command, String[] params) {

		// commands will return any string that the bot should send back as a message to
		// the command
		// CommandResponse response = executor.executeCommand(command, event, params);
		return executor.executeCommand(command, event, params).flatMap(response -> {
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
		return sendReply(event.getMessage().getChannel(), response);
	}

	public static Mono<CommandResponse> sendReply(Mono<MessageChannel> channelMono, CommandResponse response) {
		return channelMono.flatMap(channel -> {
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
	}

}
