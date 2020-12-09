package com.github.MudPitBot.command.impl;

import java.util.Set;
import java.util.Map.Entry;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.Commands;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class CommandsCommand extends Command {

	public CommandsCommand() {
		super("commands");
	}

	@Override
	public CommandResponse execute(MessageCreateEvent event, String[] params) {
		return printCommands();
	}

	/**
	 * Returns a list of all commands in the channel the message was sent.
	 * 
	 * @return List of available commands
	 */
	public CommandResponse printCommands() {
		StringBuilder sb = new StringBuilder("Available commands:");
		Set<Entry<String, Command>> entries = Commands.getEntries();
		for (Entry<String, Command> entry : entries) {
			sb.append(", ").append(Commands.COMMAND_PREFIX).append(entry.getKey());
		}
		return new CommandResponse((sb.toString().replaceAll(":,", ":")));
	}

}
