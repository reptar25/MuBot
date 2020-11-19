package com.github.MudPitBot.botCommand.commandImpl;

import com.github.MudPitBot.botCommand.CommandReceiver;
import com.github.MudPitBot.botCommand.commandInterface.Command;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class VolumeCommand extends Command {

	public VolumeCommand(CommandReceiver receiver) {
		super(receiver);
	}

	@Override
	public void execute(MessageCreateEvent event, String[] params) {
		receiver.volume(event, params);
	}

	@Override
	public String getCommandTrigger() {
		return "volume";
	}

}
