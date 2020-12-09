package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.sound.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class ClearCommand extends Command {

	public ClearCommand() {
		super("clear");
	}

	@Override
	public CommandResponse execute(MessageCreateEvent event, String[] params) {
		return clearQueue(getScheduler(event));
	}

	/**
	 * Clears the current queue of all objects
	 * 
	 * @param event The message event
	 * @return "Queue cleared" if successful, null if not
	 */
	public CommandResponse clearQueue(TrackScheduler scheduler) {
		if (scheduler != null) {
			scheduler.clearQueue();
			return new CommandResponse("Queue cleared");
		}

		return null;
	}

}
