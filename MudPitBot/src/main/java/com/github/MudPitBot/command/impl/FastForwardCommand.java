package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.core.CommandReceiver;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class FastForwardCommand extends Command {

	public FastForwardCommand(CommandReceiver receiver) {
		super(receiver);
	}

	@Override
	public CommandResponse execute(MessageCreateEvent event, String[] params) {
		return receiver.fastForward(getScheduler(event), params);
	}

	@Override
	public String getCommandTrigger() {
		return "fastforward";
	}

}