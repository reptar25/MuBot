package com.github.mubot.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class CommandExecutor {
	private static final Logger LOGGER = Loggers.getLogger(CommandExecutor.class);

	public Mono<CommandResponse> executeCommand(MessageCreateEvent event, Command command, String[] args) {
		if (args.length > 0 && args[0].equals("help")) {
			LOGGER.info("Help called for " + command.getPrimaryTrigger());
			return command.getHelp(event);
		}

		LOGGER.info("Command executed: " + event.getMessage().getContent());
		return command.execute(event, args);
	}

}