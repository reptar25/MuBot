package com.github.MudPitBot.botCommand.commandImpl;

import com.github.MudPitBot.botCommand.BotReceiver;
import com.github.MudPitBot.botCommand.commandInterface.Command;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class ClearCommand extends Command {

	public ClearCommand(BotReceiver receiver) {
		super(receiver);
	}

	@Override
	public void execute(MessageCreateEvent event) {
		receiver.clearQueue(event);
	}

}
