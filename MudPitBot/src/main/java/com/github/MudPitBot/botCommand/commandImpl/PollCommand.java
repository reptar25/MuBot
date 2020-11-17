package com.github.MudPitBot.botCommand.commandImpl;

import com.github.MudPitBot.botCommand.CommandReceiver;
import com.github.MudPitBot.botCommand.commandInterface.Command;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class PollCommand extends Command {

	public PollCommand(CommandReceiver receiver) {
		super(receiver);
	}

	@Override
	public void execute(MessageCreateEvent event) {
		receiver.poll(event);
	}

}
