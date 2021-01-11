package com.github.mubot.command.commands.music;

import static com.github.mubot.command.util.PermissionsHelper.requireSameVoiceChannel;

import java.util.function.Consumer;

import com.github.mubot.command.Command;
import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.help.CommandHelpSpec;
import com.github.mubot.music.GuildMusicManager;
import com.github.mubot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class SeekCommand extends Command {

	public SeekCommand() {
		super("seek");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireSameVoiceChannel(event).flatMap(channel -> GuildMusicManager.getScheduler(channel))
				.flatMap(scheduler -> seek(scheduler, args));
	}

	/**
	 * @param event The message event
	 * @param args  The position to move the current song to in seconds
	 * @return null
	 */
	public Mono<CommandResponse> seek(@NonNull TrackScheduler scheduler, @NonNull String[] args) {
		if (args.length > 0) {
			try {
				int positionInSeconds = Integer.parseInt(args[0]);
				scheduler.seek(positionInSeconds);
			} catch (NumberFormatException e) {
				// just ignore commands with improper number
			}
		}
		return CommandResponse.empty();
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Moves the currently playing song to the given time.").addArg("time",
				"amount of time in seconds to set the song to i.e. \"60\" will set the song to the 1 minute mark, and \"0\" would set the song back to the beginning.",
				false).addExample("60").addExample("0");
	}
}
