package com.github.mubot.command.commands.music;

import java.util.Arrays;
import java.util.function.Consumer;

import com.github.mubot.command.help.CommandHelpSpec;
import com.github.mubot.music.TrackScheduler;

public class RewindCommand extends TrackSeekingCommand {

	public RewindCommand() {
		super("rewind", Arrays.asList("rw"));
	}
	
	@Override
	protected void doSeeking(TrackScheduler scheduler, int amountInSeconds) {
		scheduler.rewind(amountInSeconds);
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Rewinds the currently playing song by the given amount of seconds.")
				.addArg("time", "amount of time in seconds to rewind", false).addExample("60");
	}


}