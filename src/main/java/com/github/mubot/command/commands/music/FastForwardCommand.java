package com.github.mubot.command.commands.music;

import java.util.Arrays;
import java.util.function.Consumer;

import com.github.mubot.command.help.CommandHelpSpec;
import com.github.mubot.music.TrackScheduler;

public class FastForwardCommand extends TrackSeekingCommand {

	public FastForwardCommand() {
		super("fastforward", Arrays.asList("ff"));
	}

	@Override
	protected void doSeeking(TrackScheduler scheduler, int amountInSeconds) {
		scheduler.fastForward(amountInSeconds);
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Fast fowards the currently playing song by the given amount of seconds.")
				.addArg("time", "amount of time in seconds to fast foward", false).addExample("60");
	}
}
