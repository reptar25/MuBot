package com.github.MudPitBot.command;

import static com.github.MudPitBot.command.CommandUtil.sendReply;

import java.util.Arrays;
import java.util.function.Predicate;

import com.github.MudPitBot.command.exceptions.CommandException;
import com.github.MudPitBot.command.util.Emoji;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.Logger;
import reactor.util.Loggers;

public class CommandListener {

	private static final Logger LOGGER = Loggers.getLogger(CommandListener.class);
	private GatewayDiscordClient client;
	private static CommandExecutor executor = new CommandExecutor();
	private static CommandListener instance;
	private static final int MAX_COMMANDS_PER_MESSAGE = 5;

	// Singleton create method
	public static CommandListener create(GatewayDiscordClient client) {
		if (instance == null)
			instance = new CommandListener(client);

		return instance;
	}

	private CommandListener(GatewayDiscordClient client) {
		this.client = client;

		setupListener();
		LOGGER.info(("Command Client created."));
	}

	/*
	 * Sets up a listeners on the event dispatcher.
	 */
	private void setupListener() {

		if (client.getEventDispatcher() != null) {
			/*
			 * Add listener for new messages being sent. Whenever a messaged is typed in
			 * chat that the bot is able to "see" should emit through the "on" flux
			 */
			client.getEventDispatcher().on(MessageCreateEvent.class)
					// ignore any messages sent from a bot
					.filter(event -> !event.getMessage().getAuthor().map(User::isBot).orElse(true))
					.flatMap(CommandListener::receiveMessage).onErrorResume(error -> {
						LOGGER.error("Error receiving message.", error);
						return Mono.empty();
					}).subscribe();
		}

	}

	/**
	 * Called when a message is created that is not from a bot. This checks the
	 * message for any commands and executes those commands
	 * 
	 * @param event the MessageCreateEvent
	 * @return Mono<Void>
	 */
	private static Mono<Void> receiveMessage(MessageCreateEvent event) {
		return Mono.justOrEmpty(event.getMessage().getContent())
				.map(content -> content.split(Commands.DEFAULT_COMMAND_PREFIX)).flatMapMany(Flux::fromArray)
				.filter(Predicate.not(String::isBlank)).take(MAX_COMMANDS_PER_MESSAGE)
				.flatMap(commandString -> Mono.justOrEmpty(Commands.get(commandString.split(" ")[0].toLowerCase()))
						.flatMap(command -> Mono.just(commandString.trim().split(" ")).flatMap(
								splitCommand -> Mono.just(Arrays.copyOfRange(splitCommand, 1, splitCommand.length)))
								.flatMap(commandArgs -> executeCommand(event, command, commandArgs)))
						.onErrorResume(CommandException.class, error -> {
							LOGGER.error(error.getMessage());

							// Send command errors back as a reply to the user who used the command
							return sendReply(event, CommandResponse.createFlat(
									Emoji.NO_ENTRY + " " + error.getUserFriendlyMessage() + " " + Emoji.NO_ENTRY));
						}).defaultIfEmpty(CommandResponse.emptyResponse()).elapsed()
						.doOnNext(TupleUtils.consumer((elapsed, response) -> LOGGER.info("{} took {} ms to complete",
								commandString.split(" ")[0].toLowerCase(), elapsed))))
				.then();
	}

	/**
	 * Processes the given command and returns the response if any
	 * 
	 * @param event   the message event
	 * @param command the command to process
	 * @param args    the parameters of the command
	 * @return the response to the command
	 */
	private static Mono<CommandResponse> executeCommand(MessageCreateEvent event, Command command, String[] args) {

		// commands will return any string that the bot should send back as a message to
		// the command
		// CommandResponse response = executor.executeCommand(command, event, args);
		return executor.executeCommand(event, command, args).flatMap(response -> {
			if (response.getSpec() == null)
				return CommandResponse.empty();

			return sendReply(event, response);
		});
	}
}