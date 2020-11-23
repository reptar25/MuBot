package com.github.MudPitBot.botCommand.commandImpl;

import com.github.MudPitBot.botCommand.CommandReceiver;
import com.github.MudPitBot.botCommand.commandInterface.Command;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class LeaveVoiceCommand extends Command {

	public LeaveVoiceCommand(CommandReceiver receiver) {
		super(receiver);
	};

	@Override
	public String execute(MessageCreateEvent event, String[] params) {
		return receiver.leave(event);
	}

	@Override
	public String getCommandTrigger() {
		return "leave";
	}

}
