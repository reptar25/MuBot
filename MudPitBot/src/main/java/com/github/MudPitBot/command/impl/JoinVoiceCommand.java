package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.core.CommandReceiver;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class JoinVoiceCommand extends Command {

	public JoinVoiceCommand(CommandReceiver receiver) {
		super(receiver, "join");
	};

	@Override
	public CommandResponse execute(MessageCreateEvent event, String[] params) {
		return receiver.join(event);
	}

}
