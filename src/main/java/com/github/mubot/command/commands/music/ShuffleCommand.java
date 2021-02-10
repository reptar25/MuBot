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
import reactor.util.annotation.NonNull;

public class ShuffleCommand extends MusicCommand {

	public ShuffleCommand() {
		super("shuffle", Arrays.asList("random"));
	}

	@Override
	protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args, TrackScheduler scheduler,
			VoiceChannel channel) {
		return shuffleQueue(scheduler);
	}

	/**
	 * Shuffles the songs currently in the queue
	 * 
	 * @param scheduler the track scheduler
	 * @return
	 */
	public Mono<CommandResponse> shuffleQueue(@NonNull TrackScheduler scheduler) {
		scheduler.shuffleQueue();
		return CommandResponse.create(EmojiHelper.SHUFFLE + " Queue shuffled " + EmojiHelper.SHUFFLE);
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Shuffles the songs that are in the queue.");
	}

}
