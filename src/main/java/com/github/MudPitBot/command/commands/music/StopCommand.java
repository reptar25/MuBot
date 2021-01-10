package com.github.MudPitBot.command.commands.music;

import static com.github.MudPitBot.command.util.Permissions.requireSameVoiceChannel;

import java.util.function.Consumer;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.help.CommandHelpSpec;
import com.github.MudPitBot.command.util.Emoji;
import com.github.MudPitBot.music.GuildMusicManager;
import com.github.MudPitBot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.NonNull;

public class StopCommand extends Command {

	private static final Logger LOGGER = Loggers.getLogger(StopCommand.class);

	public StopCommand() {
		super("stop");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireSameVoiceChannel(event).flatMap(channel -> GuildMusicManager.getScheduler(channel))
				.flatMap(scheduler -> stop(scheduler));
	}

	/**
	 * Stops the LavaPlayer if it is playing anything
	 * 
	 * @param event The message event
	 * @return "Player stopped" if successful, null if not
	 */
	public Mono<CommandResponse> stop(@NonNull TrackScheduler scheduler) {
		scheduler.getPlayer().stopTrack();
		scheduler.clearQueue();
		LOGGER.info("Stopped music");
		return CommandResponse.create(Emoji.STOP_SIGN + " Player stopped " + Emoji.STOP_SIGN);
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Stops the currently playing song and clears all songs from the queue.");
	}

}
