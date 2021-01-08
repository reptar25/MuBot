package com.github.MudPitBot.command.commands.general;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;

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
	public Mono<CommandResponse> getHelp() {
		return createCommandHelpEmbed(s -> s.setDescription("Bot replies with a simple echo message."));
	}

}
