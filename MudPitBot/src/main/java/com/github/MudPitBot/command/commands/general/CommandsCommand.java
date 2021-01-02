package com.github.MudPitBot.command.commands.general;

import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.Commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class CommandsCommand extends Command {

	public CommandsCommand() {
		super("commands");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return printCommands();
	}

	/**
	 * Returns a list of all commands in the channel the message was sent.
	 * 
	 * @return List of available commands
	 */
	public Mono<CommandResponse> printCommands() {
		StringBuilder sb = new StringBuilder("Available commands: ");
		Set<Entry<String, Command>> entries = Commands.getEntries();
		sb.append(entries.parallelStream().map(entry -> String.format("%s%s", Commands.DEFAULT_COMMAND_PREFIX, entry.getKey()))
				.sorted().collect(Collectors.joining(", ")).toString());
		return CommandResponse.create(sb.toString());
	}

}
