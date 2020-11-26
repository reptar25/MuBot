package com.github.MudPitBot.botCommand.commandImpl;

import com.github.MudPitBot.botCommand.CommandReceiver;
import com.github.MudPitBot.botCommand.commandInterface.Command;
import com.github.MudPitBot.botCommand.commandInterface.CommandResponse;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class EchoCommand extends Command {
	
	public EchoCommand(CommandReceiver receiver) {
		super(receiver);
	};

	@Override
	public CommandResponse execute(MessageCreateEvent event, String[] params) {
		return receiver.echo();
	}

	@Override
	public String getCommandTrigger() {
		return "echo";
	}

}
