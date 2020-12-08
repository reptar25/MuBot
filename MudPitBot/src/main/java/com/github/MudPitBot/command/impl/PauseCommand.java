package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.core.CommandReceiver;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class PauseCommand extends Command {

	public PauseCommand(CommandReceiver receiver) {
		super(receiver, "pause");
	}

	@Override
	public CommandResponse execute(MessageCreateEvent event, String[] params) {
		return receiver.pause(getScheduler(event));
	}

}
