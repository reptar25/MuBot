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

public class RepeatCommand extends Command {

	public RepeatCommand() {
		super("repeat");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireSameVoiceChannel(event).flatMap(channel -> GuildMusicManager.getScheduler(channel))
				.flatMap(scheduler -> repeat(scheduler));
	}

	private Mono<CommandResponse> repeat(@NonNull TrackScheduler scheduler) {
		boolean repeatEnabled = scheduler.repeatEnabled();
		String response;

		if (repeatEnabled)
			response = EmojiHelper.NO_ENTRY + " Repeat disabled " + EmojiHelper.NO_ENTRY;
		else
			response = EmojiHelper.REPEAT + " Repeat enabled " + EmojiHelper.REPEAT;

		scheduler.setRepeat(!repeatEnabled);
		return CommandResponse.create(response);
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription(
				"Toggles repeating the currently playing song. Use this command again to enable/disable repeating.");
	}

}
