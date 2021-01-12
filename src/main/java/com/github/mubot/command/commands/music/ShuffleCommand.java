package com.github.mubot.command.commands.music;

import static com.github.mubot.command.util.PermissionsHelper.requireSameVoiceChannel;

import java.util.Arrays;
import java.util.function.Consumer;

import com.github.mubot.command.Command;
import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.help.CommandHelpSpec;
import com.github.mubot.command.util.EmojiHelper;
import com.github.mubot.music.GuildMusicManager;
import com.github.mubot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class ShuffleCommand extends Command {

	public ShuffleCommand() {
		super("shuffle", Arrays.asList("random"));
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireSameVoiceChannel(event).flatMap(channel -> GuildMusicManager.getScheduler(channel))
				.flatMap(scheduler -> shuffleQueue(scheduler));
	}

	/**
	 * Shuffles the songs currently in the queue
	 * 
	 * @param event The message event
	 * @return null
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
