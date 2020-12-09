package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.sound.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class RewindCommand extends Command {

	public RewindCommand() {
		super("rewind");
	}

	@Override
	public CommandResponse execute(MessageCreateEvent event, String[] params) {
		return rewind(getScheduler(event), params);
	}

	/**
	 * @param event  The message event
	 * @param params The amount of time in seconds to rewind
	 * @return null
	 */
	public CommandResponse rewind(TrackScheduler scheduler, String[] params) {
		if (scheduler != null && params != null) {
			if (params.length > 0) {
				try {
					int amountInSeconds = Integer.parseInt(params[0]);
					scheduler.rewind(amountInSeconds);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

}
