package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class EchoCommand extends Command {

	public EchoCommand() {
		super("echo");
	};

	@Override
	public CommandResponse execute(MessageCreateEvent event, String[] params) {
		return echo();
	}

	/**
	 * Bot replies with a simple echo message
	 * 
	 * @return "echo!"
	 */
	public CommandResponse echo() {
		return new CommandResponse("echo!");
	}

}
