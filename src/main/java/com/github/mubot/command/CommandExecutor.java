package com.github.mubot.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class CommandExecutor {

	public Mono<CommandResponse> executeCommand(MessageCreateEvent event, Command command, String[] args) {
		if (args.length > 0 && args[0].equals("help"))
			return command.getHelp();
		return command.execute(event, args);
	}

}