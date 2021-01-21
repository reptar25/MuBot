package com.github.mubot.command;

import static com.github.mubot.command.util.CommandUtil.sendReply;
import static com.github.mubot.command.util.CommandUtil.getEscapedGuildPrefixFromEvent;
import static com.github.mubot.command.util.CommandUtil.getRawGuildPrefixFromEvent;

import java.util.Arrays;
import java.util.function.Predicate;

import com.github.mubot.command.exceptions.CommandException;
import com.github.mubot.command.util.EmojiHelper;
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
	private static CommandListener instance;
	private static final int MAX_COMMANDS_PER_MESSAGE = 5;
	private static final CommandExecutor commandExecutor = new CommandExecutor();

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
				.map(content -> content.split(" (?=" + getEscapedGuildPrefixFromEvent(event) + ")"))
				.flatMapMany(Flux::fromArray).map(content -> {
					if (content.startsWith(getRawGuildPrefixFromEvent(event)))
						return content.replaceAll(getEscapedGuildPrefixFromEvent(event), "");
					else
						return "";
				}).filter(Predicate.not(String::isBlank)).take(MAX_COMMANDS_PER_MESSAGE)
				.flatMap(commandString -> Mono
						.justOrEmpty(CommandsHelper.get(commandString.split(" ")[0].toLowerCase()))
						.flatMap(command -> Mono.just(commandString.trim().split(" ")).flatMap(
								splitCommand -> Mono.just(Arrays.copyOfRange(splitCommand, 1, splitCommand.length)))
								.flatMap(commandArgs -> executeCommand(event, command, commandArgs)))
						.onErrorResume(CommandException.class, error -> {
							LOGGER.error(error.getMessage());

							// Send command errors back as a reply to the user who used the command
							return sendReply(event, CommandResponse.createFlat(EmojiHelper.NO_ENTRY + " "
									+ error.getUserFriendlyMessage() + " " + EmojiHelper.NO_ENTRY)).then();
						}))
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
	private static Mono<Void> executeCommand(MessageCreateEvent event, Command command, String[] args) {
		return commandExecutor.executeCommand(event, command, args).flatMap(response -> {
			return sendReply(event, response);
		}).defaultIfEmpty(CommandResponse.emptyFlat()).elapsed().doOnNext(TupleUtils.consumer(
				(elapsed, response) -> LOGGER.info("{} took {} ms to complete", command.getPrimaryTrigger(), elapsed)))
				.then();
	}
}