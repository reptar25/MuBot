package com.github.MudPitBot.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

/**
 * Invoker class for command pattern. An invoker is an object that knows how to
 * execute a given command but doesn't know how the command has been
 * implemented. It only knows the command's interface.
 * https://www.baeldung.com/java-command-pattern
 */
public class CommandExecutor {

	public Mono<CommandResponse> executeCommand(MessageCreateEvent event, Command command, String[] args) {
		return command.execute(event, args);
	}

}
