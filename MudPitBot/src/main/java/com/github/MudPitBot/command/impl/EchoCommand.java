package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.core.CommandReceiver;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class EchoCommand extends Command {
	
	public EchoCommand(CommandReceiver receiver) {
		super(receiver);
	};

	@Override
	public CommandResponse execute(MessageCreateEvent event, String[] params) {
		return receiver.echo();
	}

	@Override
	public String getCommandTrigger() {
		return "echo";
	}

}
