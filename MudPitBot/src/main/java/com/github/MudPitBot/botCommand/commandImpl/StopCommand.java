package com.github.MudPitBot.botCommand.commandImpl;

import com.github.MudPitBot.botCommand.BotReceiver;
import com.github.MudPitBot.botCommand.commandInterface.Command;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class StopCommand extends Command {

	public StopCommand(BotReceiver receiver) {
		super(receiver);
	}

	@Override
	public void execute(MessageCreateEvent event) {
		receiver.stop(event);
	}

}
