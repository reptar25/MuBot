package com.github.mubot.command.commands.music;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.help.CommandHelpSpec;
import com.github.mubot.music.GuildMusicManager;
import com.github.mubot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.VoiceChannel;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class VolumeCommand extends MusicCommand {

	public VolumeCommand() {
		super("volume", Arrays.asList("vol"));
	}

	@Override
	protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args, TrackScheduler scheduler,
			VoiceChannel channel) {
		return volume(args, scheduler);
	}

	/**
	 * Sets the volume of the
	 * {@link com.sedmelluq.discord.lavaplayer.player.AudioPlayer}
	 * 
	 * @param args      the volume to set to, reset to reset to default, or nothing
	 *                  to get current volume
	 * @param scheduler the track scheduler
	 * @return
	 */
	public Mono<CommandResponse> volume(@NonNull String[] args, @NonNull TrackScheduler scheduler) {
		StringBuilder sb = new StringBuilder();
		if (args.length == 0) {
			return CommandResponse
					.create(sb.append("Volume is currently " + scheduler.getPlayer().getVolume()).toString());
		} else if (args[0].equalsIgnoreCase("reset")) {
			scheduler.getPlayer().setVolume(GuildMusicManager.DEFAULT_VOLUME);
			return CommandResponse.create(sb.append("Volume reset to default").toString());
		}

		if (Pattern.matches("^[1-9][0-9]?$|^100$", args[0])) {
			int volume = Integer.parseInt(args[0]);
			sb.append("Changing volume from ").append(scheduler.getPlayer().getVolume()).append(" to ").append(volume);
			scheduler.getPlayer().setVolume(volume);
			return CommandResponse.create(sb.toString());
		} else
			return CommandResponse.create("Invalid volume amount");
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription(
				"Changes the volume to the given amount, or to the default amount if reset is given, or no argument to get the current volume.")
				.addArg("volume||reset",
						"Volume to set the bot to from 0 to 100 or \"reset\" to reset the volume to default.", true)
				.addExample("50").addExample("reset");
	}
}