package mubot.command.commands.music;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.VoiceChannel;
import mubot.command.CommandResponse;
import mubot.command.help.CommandHelpSpec;
import mubot.command.util.EmojiHelper;
import mubot.music.TrackScheduler;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.util.Collections;
import java.util.function.Consumer;

public class ShuffleCommand extends MusicCommand {

	public ShuffleCommand() {
		super("shuffle", Collections.singletonList("random"));
	}

	@Override
	protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args, TrackScheduler scheduler,
			VoiceChannel channel) {
		return shuffleQueue(scheduler);
	}

	/**
	 * Shuffles the songs currently in the queue
	 * 
	 * @param scheduler the track scheduler
	 * @return the shuffle response
	 */
	public Mono<CommandResponse> shuffleQueue(@NonNull TrackScheduler scheduler) {
		scheduler.shuffleQueue();
		return CommandResponse.create(EmojiHelper.SHUFFLE + " Queue shuffled " + EmojiHelper.SHUFFLE);
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Shuffles the songs that are in the queue.");
	}

}
