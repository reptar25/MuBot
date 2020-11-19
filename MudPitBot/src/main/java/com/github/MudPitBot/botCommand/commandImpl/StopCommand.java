package com.github.MudPitBot.botCommand.commandImpl;

import com.github.MudPitBot.botCommand.CommandReceiver;
import com.github.MudPitBot.botCommand.commandInterface.Command;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class StopCommand extends Command {

	public StopCommand(CommandReceiver receiver) {
		super(receiver);
	}

	@Override
	public void execute(MessageCreateEvent event, String[] params) {
		receiver.stop(event);
	}

	@Override
	public String getCommandTrigger() {
		return "stop";
	}

}
