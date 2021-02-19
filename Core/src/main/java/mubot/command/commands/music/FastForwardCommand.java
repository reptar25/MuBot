package mubot.command.commands.music;

import mubot.command.help.CommandHelpSpec;
import mubot.music.TrackScheduler;

import java.util.Collections;
import java.util.function.Consumer;

public class FastForwardCommand extends TrackSeekingCommand {

	public FastForwardCommand() {
		super("fastforward", Collections.singletonList("ff"));
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
