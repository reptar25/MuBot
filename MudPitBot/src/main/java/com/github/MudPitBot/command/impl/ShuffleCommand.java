package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.sound.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class ShuffleCommand extends Command {

	public ShuffleCommand() {
		super("shuffle");
	}

	@Override
	public CommandResponse execute(MessageCreateEvent event, String[] params) {
		return shuffleQueue(getScheduler(event));
	}

	/**
	 * Shuffles the songs currently in the queue
	 * 
	 * @param event The message event
	 * @return null
	 */
	public CommandResponse shuffleQueue(TrackScheduler scheduler) {
		if (scheduler != null) {
			scheduler.shuffleQueue();
		}
		return null;
	}

}
