package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class EchoCommand extends Command {

	public EchoCommand() {
		super("echo");
	};

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] params) {
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

}
