package com.github.MudPitBot.botCommand.commandImpl;

import com.github.MudPitBot.botCommand.CommandReceiver;
import com.github.MudPitBot.botCommand.commandInterface.Command;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class CommandsCommand extends Command {

	public CommandsCommand(CommandReceiver receiver) {
		super(receiver);
	}

	@Override
	public void execute(MessageCreateEvent event, String[] params) {
		receiver.printCommands(event, params);
	}

	@Override
	public String getCommandTrigger() {
		return "commands";
	}

}
