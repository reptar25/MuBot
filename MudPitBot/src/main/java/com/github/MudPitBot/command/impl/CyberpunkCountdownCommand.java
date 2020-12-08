package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.core.CommandReceiver;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class CyberpunkCountdownCommand extends Command {

	public CyberpunkCountdownCommand(CommandReceiver receiver) {
		super(receiver, "Cyberpunk");

	}

	@Override
	public CommandResponse execute(MessageCreateEvent event, String[] params) {
		return receiver.cyberpunk();
	}

}
