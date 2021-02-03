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

public class RepeatCommand extends MusicCommand {

	public RepeatCommand() {
		super("repeat", Arrays.asList("loop"));
	}

	@Override
	protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args, TrackScheduler scheduler,
			VoiceChannel channel) {
		return repeat(scheduler);
	}

	private Mono<CommandResponse> repeat(@NonNull TrackScheduler scheduler) {
		boolean repeatEnabled = scheduler.repeatEnabled();
		String response = repeatEnabled ? EmojiHelper.NO_ENTRY + " Repeat disabled " + EmojiHelper.NO_ENTRY
				: EmojiHelper.REPEAT + " Repeat enabled " + EmojiHelper.REPEAT;

		scheduler.setRepeat(!repeatEnabled);
		return CommandResponse.create(response);
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription(
				"Toggles repeating the currently playing song. Use this command again to enable/disable repeating.");
	}

}
