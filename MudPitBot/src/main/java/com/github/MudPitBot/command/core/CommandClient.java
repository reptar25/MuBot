package com.github.MudPitBot.command.core;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.function.Predicate;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.Commands;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
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
			client.getEventDispatcher().on(MessageCreateEvent.class)
					// subscribe is like block, in that it will *request* for action
					// to be done, but instead of blocking the thread, waiting for it
					// to finish, it will just execute the results asynchronously.
					.subscribe(event -> {
						// 3.1 Message.getContent() is a String
						final String content = event.getMessage().getContent();
						// process new message to check for commands
						Mono.just(processMessage(event, content)).subscribe();
					});

		}
	};

	/**
	 * Processes the message to check for commands and execute those commands
	 * 
	 * @param event   event of the message
	 * @param content content of the message
	 */
	public Mono<Void> processMessage(MessageCreateEvent event, String content) {
		// ignore any messages sent from a bot
		if (event.getMessage().getAuthor().map(User::isBot).orElse(true)) {
			return Mono.empty();
		}
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
					// matching command found, so process and execute that command
					// copy removes the command itself from the parameters
					processCommand(event, entry.getValue(), Arrays.copyOfRange(splitCommand, 1, splitCommand.length));
					// we found a matching command so stop the loop
					break;
				}
			}
		}
		return Mono.empty();
	}

	private void processCommand(MessageCreateEvent event, Command command, String[] params) {

		// commands will return any string that the bot should send back as a message to
		// the command
		CommandResponse response = executor.executeCommand(event, command, params);

		// if there is a message to send back send it to the channel the original
		// message was sent from
		if (response != null) {
			event.getMessage().getChannel().subscribe(channel -> {
				if (response.getSpec() != null) {
					channel.createMessage(response.getSpec()).subscribe(message -> {
						// if the response contained a poll
						if (response.getPoll() != null) {
							// add reactions as vote tickers, number of reactions depends on number of
							// answers
							response.getPoll().addReactions(message);
						}
					});
				}
			});
		}
	}
}
