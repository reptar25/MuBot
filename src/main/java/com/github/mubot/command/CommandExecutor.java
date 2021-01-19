package com.github.mubot.command;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class CommandExecutor {

	public Mono<CommandResponse> executeCommand(MessageCreateEvent event, Command command, String[] args) {
		if (args.length > 0 && args[0].equals("help"))
			return command.getHelp(event.getGuildId().orElse(Snowflake.of(0)).asLong());
		return command.execute(event, args);
	}

}