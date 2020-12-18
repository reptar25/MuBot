package com.github.MudPitBot.command.commands.music;

import static com.github.MudPitBot.command.util.CommandUtil.requireSameVoiceChannel;

import java.util.List;
import java.util.function.Consumer;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.util.Emoji;
import com.github.MudPitBot.command.util.Paginator;
import com.github.MudPitBot.command.util.Paginator.Builder;
import com.github.MudPitBot.music.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Mono;

public class ViewQueueCommand extends Command {

	public ViewQueueCommand() {
		super("viewqueue");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] params) {
		return requireSameVoiceChannel(event).flatMap(channel -> getScheduler(channel))
				.flatMap(scheduler -> viewQueue(scheduler, event.getMessage().getChannel()));
	}

	/**
	 * Returns a list of the currently queued songs
	 * 
	 * @param channelMono
	 * 
	 * @param scheduler   The TrackScheduler for this guild
	 * @return List of songs in the queue, or "The queue is empty" if empty
	 */
	public Mono<CommandResponse> viewQueue(TrackScheduler scheduler, Mono<MessageChannel> channelMono) {
		if (scheduler != null) {
			// get list of songs currently in the queue
			List<AudioTrack> queue = scheduler.getQueue();
			Builder paginatorBuilder = new Paginator.Builder();
			// if the queue is not empty
			if (queue.size() > 0) {
				String[] queueEntries = new String[queue.size()];
				// print total number of songs
				paginatorBuilder.withMessageContent(getNowPlaying(scheduler) + "\n" + "There is currently "
						+ Emoji.numToEmoji(queue.size()) + " songs in the queue");
				for (int i = 0; i < queue.size(); i++) {
					AudioTrack track = queue.get(i);
					// print title and author of song on its own line
					queueEntries[i] = Emoji.numToEmoji(i + 1) + " - \"" + track.getInfo().title + "\" by "
							+ track.getInfo().author + "\n";
				}

				Paginator paginator = paginatorBuilder.withEntries(queueEntries).build();
				Consumer<? super MessageCreateSpec> spec = paginator.createMessage();
				return new CommandResponse.Builder().withCreateSpec(spec).withPaginator(paginator).build();

			} else {
				return CommandResponse.create("The queue is empty");
			}
		}
		return CommandResponse.empty();
	}

	private String getNowPlaying(TrackScheduler scheduler) {
		AudioTrackInfo info = scheduler.getNowPlaying().getInfo();
		return "\"" + info.title + "\" by \"" + info.author + "\" is currently playing";
	}
}
