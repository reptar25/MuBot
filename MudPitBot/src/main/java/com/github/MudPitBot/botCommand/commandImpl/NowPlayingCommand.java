package com.github.MudPitBot.botCommand.commandImpl;

import com.github.MudPitBot.botCommand.CommandReceiver;
import com.github.MudPitBot.botCommand.commandInterface.Command;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class NowPlayingCommand extends Command {

	public NowPlayingCommand(CommandReceiver receiver) {
		super(receiver);
	}

	@Override
	public String execute(MessageCreateEvent event, String[] params) {
		return receiver.nowPlaying(event);
	}

	@Override
	public String getCommandTrigger() {
		return "nowplaying";
	}

}
