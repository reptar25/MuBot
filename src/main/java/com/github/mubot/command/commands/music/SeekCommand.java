package com.github.mubot.command.commands.music;

import java.util.function.Consumer;

import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.help.CommandHelpSpec;
import com.github.mubot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.VoiceChannel;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class SeekCommand extends MusicCommand {

	public SeekCommand() {
		super("seek");
	}

	@Override
	protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args, TrackScheduler scheduler,
			VoiceChannel channel) {
		return seek(event, args, scheduler);
	}

	/**
	 * @param event
	 * @param event The message event
	 * @param args  The position to move the current song to in seconds
	 * @return null
	 */
	public Mono<CommandResponse> seek(MessageCreateEvent event, @NonNull String[] args,
			@NonNull TrackScheduler scheduler) {
		if (args.length > 0) {
			try {
				int positionInSeconds = Integer.parseInt(args[0]);
				scheduler.seek(positionInSeconds);
			} catch (NumberFormatException e) {
				return getHelp(event);
			}
		}
		return getHelp(event);
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Moves the currently playing song to the given time.").addArg("time",
				"amount of time in seconds to set the song to i.e. \"60\" will set the song to the 1 minute mark, and \"0\" would set the song back to the beginning.",
				false).addExample("60").addExample("0");
	}

}
