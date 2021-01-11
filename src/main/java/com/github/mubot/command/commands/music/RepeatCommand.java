package com.github.mubot.command.commands.music;

import static com.github.mubot.command.util.PermissionsHelper.requireSameVoiceChannel;

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
