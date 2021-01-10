package com.github.mudpitbot.command.commands.general;

import java.util.function.Consumer;

import com.github.mudpitbot.command.Command;
import com.github.mudpitbot.command.CommandResponse;
import com.github.mudpitbot.command.help.CommandHelpSpec;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class EchoCommand extends Command {

	public EchoCommand() {
		super("echo");
	};

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return echo();
	}

	/**
	 * Bot replies with a simple echo message
	 * 
	 * @return "echo!"
	 */
	public Mono<CommandResponse> echo() {
		return CommandResponse.create("echo!");
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Bot replies with a simple echo message.");
	}

}
