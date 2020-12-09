package com.github.MudPitBot.command.impl;

import java.util.regex.Pattern;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.sound.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class VolumeCommand extends Command {

	public VolumeCommand() {
		super("volume");
	}

	@Override
	public CommandResponse execute(MessageCreateEvent event, String[] params) {
		return volume(getScheduler(event), params);
	}
	
	/**
	 * Sets the volume of the
	 * {@link com.sedmelluq.discord.lavaplayer.player.AudioPlayer}
	 * 
	 * @param event  The message event
	 * @param params The new volume setting
	 * @return Responds with new volume setting
	 */
	public CommandResponse volume(TrackScheduler scheduler, String[] params) {
		if (scheduler != null && params != null) {

			StringBuilder sb = new StringBuilder();
			if (params.length == 0) {
				return new CommandResponse(
						sb.append("Volume is currently " + scheduler.getPlayer().getVolume()).toString());
			} else if (params[0].equalsIgnoreCase("reset")) {
				scheduler.getPlayer().setVolume(TrackScheduler.DEFAULT_VOLUME);
				return new CommandResponse(sb.append("Volume reset to default").toString());
			}

			if (Pattern.matches("^[1-9][0-9]?$|^100$", params[0])) {
				int volume = Integer.parseInt(params[0]);
				sb.append("Changing volume from ").append(scheduler.getPlayer().getVolume()).append(" to ")
						.append(volume);
				scheduler.getPlayer().setVolume(volume);
				return new CommandResponse(sb.toString());

			}
		}
		return null;
	}

}
