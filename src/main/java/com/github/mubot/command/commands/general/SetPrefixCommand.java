package com.github.mubot.command.commands.general;

import java.util.function.Consumer;

import com.github.mubot.command.Command;
import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.help.CommandHelpSpec;
import com.github.mubot.database.DatabaseManager;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class SetPrefixCommand extends Command {

	public SetPrefixCommand() {
		super("setprefix");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return prefix(event, args);
	}

	private Mono<CommandResponse> prefix(MessageCreateEvent event, String[] args) {
		if (args.length >= 1) {

			DatabaseManager.getPrefixCollection().setPrefix(event.getGuildId().get().asLong(), args[0])
					.onErrorResume(error -> Mono.empty()).subscribe();
			return CommandResponse.create("Set guild command prefix to " + args[0]);
		}
		return CommandResponse.empty();
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Set or get the command-prefix of this server.")
				.addArg("prefix", "New prefix for bot-commands.", false).addExample("$").addExample("!");
	}

}
