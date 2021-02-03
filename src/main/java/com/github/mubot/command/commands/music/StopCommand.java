package com.github.mubot.command.commands.music;

import java.util.Arrays;
import java.util.function.Consumer;

import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.help.CommandHelpSpec;
import com.github.mubot.command.util.EmojiHelper;
import com.github.mubot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.VoiceChannel;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.NonNull;

public class StopCommand extends MusicCommand {

	private static final Logger LOGGER = Loggers.getLogger(StopCommand.class);

	public StopCommand() {
		super("stop", Arrays.asList("random"));
	}

	@Override
	protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args, TrackScheduler scheduler,
			VoiceChannel channel) {
		return stop(scheduler);
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
		return CommandResponse.create(EmojiHelper.STOP_SIGN + " Player stopped " + EmojiHelper.STOP_SIGN);
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Stops the currently playing song and clears all songs from the queue.");
	}

}
