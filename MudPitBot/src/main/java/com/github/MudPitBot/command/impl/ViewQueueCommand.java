package com.github.MudPitBot.command.impl;

import java.util.List;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.sound.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public class ViewQueueCommand extends Command {

	public ViewQueueCommand() {
		super("viewqueue");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] params) {
		return getScheduler(event).flatMap(scheduler -> {
			return viewQueue(scheduler);
		});
	}

	/**
	 * Returns a list of the currently queued songs
	 * 
	 * @param event The message event
	 * @return List of songs in the queue, or "The queue is empty" if empty
	 */
	public Mono<CommandResponse> viewQueue(TrackScheduler scheduler) {
		if (scheduler != null) {
			// get list of songs currently in the queue
			List<AudioTrack> queue = scheduler.getQueue();
			StringBuilder sb = new StringBuilder();
			// if the queue is not empty
			if (queue.size() > 0) {
				// print total number of songs
				sb.append("Number of songs in queue: ").append(queue.size()).append("\n");
				for (int i = 0; i < queue.size(); i++) {
					AudioTrack track = queue.get(i);
					// print title and author of song on its own line
					sb.append("(#").append(i + 1).append(")").append("\"").append(track.getInfo().title).append("\"")
							.append(" by ").append(track.getInfo().author).append("\n");
				}
			} else {
				sb.append("The queue is empty");
			}

			String retString = sb.toString();

			// if the message is longer than 2000 character, trim it so that its not over
			// the max character limit.
			if (sb.toString().length() >= Message.MAX_CONTENT_LENGTH)
				retString = sb.substring(0, Message.MAX_CONTENT_LENGTH - 1);

			return Mono.just(new CommandResponse(retString));
		}
		return Mono.empty();
	}
}
