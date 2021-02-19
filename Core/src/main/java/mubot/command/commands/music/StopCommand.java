package mubot.command.commands.music;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.VoiceChannel;
import mubot.command.CommandResponse;
import mubot.command.help.CommandHelpSpec;
import mubot.command.util.EmojiHelper;
import mubot.music.TrackScheduler;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.NonNull;

import java.util.Collections;
import java.util.function.Consumer;

public class StopCommand extends MusicCommand {

	private static final Logger LOGGER = Loggers.getLogger(StopCommand.class);

	public StopCommand() {
		super("stop", Collections.singletonList("random"));
	}

	@Override
	protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args, TrackScheduler scheduler,
			VoiceChannel channel) {
		return stop(scheduler);
	}

	/**
	 * Stops the LavaPlayer if it is playing anything
	 * 
	 * @param scheduler the track scheduler
	 * @return the response to stopping
	 */
	public Mono<CommandResponse> stop(@NonNull TrackScheduler scheduler) {
		scheduler.getPlayer().stopTrack();
		scheduler.clearQueue();
		LOGGER.info("Stopped music");
		return CommandResponse.create(EmojiHelper.STOP_SIGN + " Player stopped " + EmojiHelper.STOP_SIGN);
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Stops the currently playing song and clears all songs from the queue.");
	}

}
