package com.github.MudPitBot.botCommand.commandImpl;

import com.github.MudPitBot.botCommand.CommandReceiver;
import com.github.MudPitBot.botCommand.commandInterface.Command;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class MuteCommand extends Command {

	public MuteCommand(CommandReceiver receiver) {
		super(receiver);
	}

	@Override
	public String execute(MessageCreateEvent event, String[] params) {
		return receiver.mute(event);
	}

	@Override
	public String getCommandTrigger() {
		return "mute";
	}

}
