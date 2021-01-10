package com.github.mudpitbot.command.commands.music;

import static com.github.mudpitbot.command.util.PermissionsHelper.requireSameVoiceChannel;

import java.util.function.Consumer;

import com.github.mudpitbot.command.Command;
import com.github.mudpitbot.command.CommandResponse;
import com.github.mudpitbot.command.help.CommandHelpSpec;
import com.github.mudpitbot.command.util.EmojiHelper;
import com.github.mudpitbot.music.GuildMusicManager;
import com.github.mudpitbot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class ShuffleCommand extends Command {

	public ShuffleCommand() {
		super("shuffle");
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
