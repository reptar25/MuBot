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
			response = Emoji.NO_ENTRY + " Repeat disabled " + Emoji.NO_ENTRY;
		else
			response = Emoji.REPEAT + " Repeat enabled " + Emoji.REPEAT;

		scheduler.setRepeat(!repeatEnabled);
		return CommandResponse.create(response);
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription(
				"Toggles repeating the currently playing song. Use this command again to enable/disable repeating.");
	}

}
