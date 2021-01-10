package com.github.mudpitbot.command.commands.music;

import static com.github.mudpitbot.command.util.PermissionsHelper.requireSameVoiceChannel;

import java.util.function.Consumer;

import com.github.mudpitbot.command.Command;
import com.github.mudpitbot.command.CommandResponse;
import com.github.mudpitbot.command.help.CommandHelpSpec;
import com.github.mudpitbot.music.GuildMusicManager;
import com.github.mudpitbot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class FastForwardCommand extends Command {

	public FastForwardCommand() {
		super("fastforward");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireSameVoiceChannel(event).flatMap(channel -> GuildMusicManager.getScheduler(channel))
				.flatMap(scheduler -> fastForward(scheduler, args));
	}

	/**
	 * @param event The message event
	 * @param args  The amount of time in seconds to fast forward
	 * @return null
	 */
	public Mono<CommandResponse> fastForward(@NonNull TrackScheduler scheduler, @NonNull String[] args) {
		if (args.length > 0) {
			try {
				int amountInSeconds = Integer.parseInt(args[0]);
				scheduler.fastForward(amountInSeconds);
			} catch (NumberFormatException e) {
				// just ignore commands with improper number
			}
		}
		return CommandResponse.empty();
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Fast fowards the currently playing song by the given amount of seconds.")
				.addArg("time", "amount of time in seconds to fast foward", false).addExample("60");
	}

}
