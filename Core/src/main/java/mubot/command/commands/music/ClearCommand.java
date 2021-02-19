package mubot.command.commands.music;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.VoiceChannel;
import mubot.command.CommandResponse;
import mubot.command.help.CommandHelpSpec;
import mubot.music.TrackScheduler;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.util.function.Consumer;

public class ClearCommand extends MusicCommand {

	public ClearCommand() {
		super("clear");
	}

	@Override
	protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args, TrackScheduler scheduler,
			VoiceChannel channel) {
		return clearQueue(scheduler);
	}


	/**
	 * Clears the current queue of all tracks
	 * @param scheduler the track scheduler to clear
	 * @return the response to clearing the queue
	 */
	public Mono<CommandResponse> clearQueue(@NonNull TrackScheduler scheduler) {
		scheduler.clearQueue();
		return CommandResponse.create("Queue cleared");
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Clears the queue of all songs.");
	}

}
