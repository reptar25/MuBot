package com.github.MudPitBot.core;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;

import discord4j.core.event.domain.message.MessageCreateEvent;

/**
 * Invoker class for command pattern. An invoker is an object that knows how to
 * execute a given command but doesn't know how the command has been
 * implemented. It only knows the command's interface.
 * https://www.baeldung.com/java-command-pattern
 */
public class CommandExecutor {

	public CommandResponse executeCommand(MessageCreateEvent event, Command command, String[] params) {
		return command.execute(event, params);
	}

}
