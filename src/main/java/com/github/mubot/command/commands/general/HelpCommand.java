package com.github.mubot.command.commands.general;

import java.util.Arrays;
import java.util.Map.Entry;

import static com.github.mubot.command.util.CommandUtil.getGuildPrefixFromEvent;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.mubot.command.Command;
import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.CommandsHelper;
import com.github.mubot.command.help.CommandHelpSpec;
import static com.github.mubot.command.util.PermissionsHelper.requireNotPrivate;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class HelpCommand extends Command {

	public HelpCommand() {
		super("help", Arrays.asList("commands", "h"));
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireNotPrivate(event).flatMap(ignored -> help(event));
	}

	/**
	 * Returns a list of all commands in the channel the message was sent.
	 * 
	 * @param event
	 * 
	 * @return List of available commands
	 */
	public Mono<CommandResponse> help(MessageCreateEvent event) {
		Set<Entry<String, Command>> entries = CommandsHelper.getEntries();
		String commands = entries.stream()
				.map(entry -> String.format("%n%s%s", getGuildPrefixFromEvent(event),
						entry.getValue().getPrimaryTrigger()))
				.distinct().sorted().collect(Collectors.joining()).toString();

		return CommandResponse.create(message -> message.setEmbed(
				embed -> embed.setTitle("Use ***help*** with any command to get more information on that command.")
						.addField("Available commands: ", commands, false)));
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Displays a list of available commands you can use with the bot.");
	}

}
