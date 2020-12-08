package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.core.CommandReceiver;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class ClearCommand extends Command {

	public ClearCommand(CommandReceiver receiver) {
		super(receiver, "clear");
	}

	@Override
	public CommandResponse execute(MessageCreateEvent event, String[] params) {
		return receiver.clearQueue(getScheduler(event));
	}

}
