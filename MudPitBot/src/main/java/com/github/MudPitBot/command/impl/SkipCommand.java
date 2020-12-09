package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.sound.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class SkipCommand extends Command {

	public SkipCommand() {
		super("skip");
	}

	@Override
	public CommandResponse execute(MessageCreateEvent event, String[] params) {
		return skip(getScheduler(event));
	}

	/**
	 * Stops the current song and plays the next in queue if there is any
	 * 
	 * @param event The message event
	 * @return The message event
	 */
	public CommandResponse skip(TrackScheduler scheduler) {
		if (scheduler != null) {
			scheduler.nextTrack();
		}

		return null;
	}

}
