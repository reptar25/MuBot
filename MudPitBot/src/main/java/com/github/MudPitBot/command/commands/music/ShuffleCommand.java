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
		return CommandResponse.create(Emoji.SHUFFLE + " Queue shuffled " + Emoji.SHUFFLE);
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Shuffles the songs that are in the queue.");
	}

}
