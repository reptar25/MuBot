package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.core.CommandReceiver;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class SeekCommand extends Command {

	public SeekCommand(CommandReceiver receiver) {
		super(receiver);
	}

	@Override
	public CommandResponse execute(MessageCreateEvent event, String[] params) {
		return receiver.seek(getScheduler(event), params);
	}

	@Override
	public String getCommandTrigger() {
		return "seek";
	}

}
