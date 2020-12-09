package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.sound.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.util.Logger;
import reactor.util.Loggers;

public class StopCommand extends Command {

	private static final Logger LOGGER = Loggers.getLogger(StopCommand.class);
	
	public StopCommand() {
		super("stop");
	}

	@Override
	public CommandResponse execute(MessageCreateEvent event, String[] params) {
		return stop(getScheduler(event));
	}

	/**
	 * Stops the LavaPlayer if it is playing anything
	 * 
	 * @param event The message event
	 * @return "Player stopped" if successful, null if not
	 */
	public CommandResponse stop(TrackScheduler scheduler) {
		if (scheduler != null) {
			scheduler.getPlayer().stopTrack();
			scheduler.clearQueue();
			LOGGER.info("Stopped music");
			return new CommandResponse("Player stopped");
		}

		return null;
	}

}
