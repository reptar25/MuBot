package com.github.mubot.command.commands.music;

import static com.github.mubot.command.util.PermissionsHelper.requireSameVoiceChannel;

import java.util.Arrays;
import java.util.function.Consumer;

import com.github.mubot.command.Command;
import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.help.CommandHelpSpec;
import com.github.mubot.music.GuildMusicManager;
import com.github.mubot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class FastForwardCommand extends Command {

	public FastForwardCommand() {
		super("fastforward", Arrays.asList("ff"));
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireSameVoiceChannel(event).flatMap(channel -> GuildMusicManager.getScheduler(channel))
				.flatMap(scheduler -> fastForward(event, scheduler, args));
	}

	/**
	 * @param event
	 * @param event The message event
	 * @param args  The amount of time in seconds to fast forward
	 * @return null
	 */
	public Mono<CommandResponse> fastForward(MessageCreateEvent event, @NonNull TrackScheduler scheduler,
			@NonNull String[] args) {
		if (args.length > 0) {
			try {
				int amountInSeconds = Integer.parseInt(args[0]);
				scheduler.fastForward(amountInSeconds);
			} catch (NumberFormatException e) {
				// just ignore commands with improper number
			}
		}
		return getHelp(event);
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Fast fowards the currently playing song by the given amount of seconds.")
				.addArg("time", "amount of time in seconds to fast foward", false).addExample("60");
	}

}
