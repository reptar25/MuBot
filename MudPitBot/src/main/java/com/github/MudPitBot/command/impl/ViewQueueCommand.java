package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.core.CommandReceiver;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class ViewQueueCommand extends Command {

	public ViewQueueCommand(CommandReceiver receiver) {
		super(receiver);
	}

	@Override
	public CommandResponse execute(MessageCreateEvent event, String[] params) {
		return receiver.viewQueue(getScheduler(event));
	}

	@Override
	public String getCommandTrigger() {
		return "viewqueue";
	}

}
