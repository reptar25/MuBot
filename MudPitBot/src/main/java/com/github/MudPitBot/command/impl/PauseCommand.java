package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.sound.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class PauseCommand extends Command {

	public PauseCommand() {
		super("pause");
	}

	@Override
	public CommandResponse execute(MessageCreateEvent event, String[] params) {
		return pause(getScheduler(event));
	}

	/**
	 * Pauses/unpauses the player
	 * 
	 * @param event The message event
	 * @return null
	 */
	public CommandResponse pause(TrackScheduler scheduler) {
		if (scheduler != null)
			scheduler.pause(!scheduler.isPaused());

		return null;
	}
}
