package com.github.mubot.command.commands.music;

import static com.github.mubot.command.util.PermissionsHelper.requireSameVoiceChannel;

import java.util.function.Consumer;
import java.util.regex.Pattern;

import com.github.mubot.command.Command;
import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.help.CommandHelpSpec;
import com.github.mubot.music.GuildMusicManager;
import com.github.mubot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class VolumeCommand extends Command {

	public VolumeCommand() {
		super("volume");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireSameVoiceChannel(event).flatMap(channel -> GuildMusicManager.getScheduler(channel))
				.flatMap(scheduler -> volume(scheduler, args));
	}

	/**
	 * Sets the volume of the
	 * {@link com.sedmelluq.discord.lavaplayer.player.AudioPlayer}
	 * 
	 * @param event The message event
	 * @param args  The new volume setting
	 * @return Responds with new volume setting
	 */
	public Mono<CommandResponse> volume(@NonNull TrackScheduler scheduler, @NonNull String[] args) {
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
