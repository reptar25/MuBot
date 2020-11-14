package com.github.MudPitBot.botCommand.commandImpl;

import com.github.MudPitBot.botCommand.CommandReceiver;
import com.github.MudPitBot.botCommand.commandInterface.Command;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class LeaveVoiceCommand extends Command {

	public LeaveVoiceCommand(CommandReceiver receiver) {
		super(receiver);
	};

	@Override
	public void execute(MessageCreateEvent event) {
		receiver.leave(event);
	}

}
