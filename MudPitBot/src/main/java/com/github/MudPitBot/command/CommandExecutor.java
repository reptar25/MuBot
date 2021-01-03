package com.github.MudPitBot.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class CommandExecutor {

	public Mono<CommandResponse> executeCommand(MessageCreateEvent event, Command command, String[] args) {
		return command.execute(event, args);
	}

}
