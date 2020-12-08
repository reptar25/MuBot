package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.core.CommandReceiver;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class StopCommand extends Command {

	public StopCommand(CommandReceiver receiver) {
		super(receiver, "stop");
	}

	@Override
	public CommandResponse execute(MessageCreateEvent event, String[] params) {
		return receiver.stop(getScheduler(event));
	}

}
