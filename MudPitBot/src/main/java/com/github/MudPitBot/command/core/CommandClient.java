package com.github.MudPitBot.command.core;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.Commands;
import com.github.MudPitBot.command.misc.MuteHelper;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;
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
		MuteHelper.create(client);
		LOGGER.info(("Client created."));
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

						// print out new message to logs
						logMessage(event, content);

						// process new message to check for commands
						processMessage(event, content);
					});

		}
	};

	/**
	 * Processes the message to check for commands and execute those commands
	 * 
	 * @param event   event of the message
	 * @param content content of the message
	 */
	public void processMessage(MessageCreateEvent event, String content) {

		// ignore any messages sent from a bot
		Mono.justOrEmpty(event.getMessage().getAuthor()).filter(Predicate.not(User::isBot)).subscribe(user -> {
			// split content at ! to allow for compound commands (more
			// than 1 command in 1 message)
			// this regex splits at !, but doesn't remove it from the resulting string
			String[] commands = content.split("(?=" + Commands.COMMAND_PREFIX + ")");
			for (String command : commands) {
				command = command.trim();
				for (final Entry<String, Command> entry : Commands.getEntries()) {
					// We will be using ! as our "prefix" to any command in the system.
					String[] splitCommand = command.split(" ");
					if (splitCommand[0].toLowerCase()
							.startsWith(Commands.COMMAND_PREFIX + entry.getKey().toLowerCase())) {
						String[] params = Arrays.copyOfRange(splitCommand, 1, splitCommand.length);

						// commands will return any string that the bot should send back as a message to
						// the command
						CommandResponse response = executor.executeCommand(event, entry.getValue(), params);

						// if there is a message to send back send it to the channel the original
						// message was sent from
						if (response != null) {
							event.getMessage().getChannel().subscribe(channel -> {
								Message message = null;
								if (response.getSpec() != null) {
									message = channel.createMessage(response.getSpec()).block();
								}

								if (response.getPoll() != null && message != null) {
									// add reactions as vote tickers, number of reactions depends on number of
									// answers
									response.getPoll().addReactions(message);
								}
							});
						}
						break;
					}
				}
			}
		});

	}

	/**
	 * Formats and logs the message content and author
	 * 
	 * @param event   even of the message
	 * @param content content of the message
	 */
	public void logMessage(MessageCreateEvent event, String content) {
		StringBuilder sb = new StringBuilder("New message created: ");

		Optional<Member> member = event.getMember();

		// add the user name and put the message in quotes
		if (member.isPresent()) {
			sb.append(member.get().getUsername());
		} else {
			event.getMessage().getAuthor().ifPresent(a -> sb.append(a.getUsername()));
		}
		sb.append(" - \"").append(content).append("\"");
		LOGGER.info(sb.toString());
	}
}
