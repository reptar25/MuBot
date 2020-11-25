package com.github.MudPitBot.botCommand.commandImpl;

import com.github.MudPitBot.botCommand.CommandReceiver;
import com.github.MudPitBot.botCommand.commandInterface.Command;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class CyberpunkCountdownCommand extends Command {

	public CyberpunkCountdownCommand(CommandReceiver receiver) {
		super(receiver);

	}

	@Override
	public String execute(MessageCreateEvent event, String[] params) {
		return receiver.cyberpunk();
	}

	@Override
	public String getCommandTrigger() {

		return "Cyberpunk";
	}

}
