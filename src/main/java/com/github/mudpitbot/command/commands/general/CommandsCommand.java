package com.github.mudpitbot.command.commands.general;

import java.util.Map.Entry;

import static com.github.mudpitbot.command.util.CommandUtil.DEFAULT_COMMAND_PREFIX;
import static com.github.mudpitbot.command.util.CommandUtil.DEFAULT_EMBED_COLOR;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.mudpitbot.command.Command;
import com.github.mudpitbot.command.CommandResponse;
import com.github.mudpitbot.command.Commands;
import com.github.mudpitbot.command.help.CommandHelpSpec;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class CommandsCommand extends Command {

	public CommandsCommand() {
		super("commands");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return commands();
	}

	/**
	 * Returns a list of all commands in the channel the message was sent.
	 * 
	 * @return List of available commands
	 */
	public Mono<CommandResponse> commands() {
		Set<Entry<String, Command>> entries = Commands.getEntries();
		String commands = entries.stream().map(entry -> String.format("%n%s%s", DEFAULT_COMMAND_PREFIX, entry.getKey()))
				.sorted().collect(Collectors.joining()).toString();

		return CommandResponse.create(message -> message.setEmbed(embed -> embed.setColor(DEFAULT_EMBED_COLOR)
				.setTitle("Use ***help*** with any command to get more information on that command.")
				.addField("Available commands: ", commands, false)));
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Displays a list of available commands you can use with the bot.");
	}

}
