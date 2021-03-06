package com.github.mubot.command.commands.music;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.help.CommandHelpSpec;
import com.github.mubot.command.menu.menus.Paginator;
import com.github.mubot.command.menu.menus.Paginator.Builder;
import com.github.mubot.command.util.CommandUtil;
import com.github.mubot.command.util.EmojiHelper;
import com.github.mubot.music.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class ViewQueueCommand extends MusicPermissionCommand {

	public ViewQueueCommand() {
		super("viewqueue", Arrays.asList("vq", "queue", "q"), Permission.MANAGE_MESSAGES);
	}

	@Override
	protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args, TrackScheduler scheduler,
			VoiceChannel channel) {
		return viewQueue(scheduler, event.getMessage().getChannel());
	}

	/**
	 * Returns a list of the currently queued songs
	 * 
	 * @param channelMono
	 * 
	 * @param scheduler   The TrackScheduler for this guild
	 * @return List of songs in the queue, or "The queue is empty" if empty
	 */
	public Mono<CommandResponse> viewQueue(@NonNull TrackScheduler scheduler, Mono<MessageChannel> channelMono) {
		// get list of songs currently in the queue
		List<AudioTrack> queue = scheduler.getQueue();
		Builder paginatorBuilder = new Paginator.Builder();
		// if the queue is not empty
		if (queue.size() > 0) {
			String[] queueEntries = new String[queue.size()];
			// print total number of songs
			paginatorBuilder.withMessageContent("Currently playing: " + CommandUtil.trackInfo(scheduler.getNowPlaying())
					+ "\n" + "There are currently " + EmojiHelper.numToEmoji(queue.size()) + " songs in the queue");
			for (int i = 0; i < queue.size(); i++) {
				AudioTrack track = queue.get(i);
				// print title and author of song on its own line
				queueEntries[i] = EmojiHelper.numToEmoji(i + 1) + " - " + CommandUtil.trackInfo(track) + "\n";
			}

			Paginator paginator = paginatorBuilder.withEntries(queueEntries).build();
			Consumer<? super MessageCreateSpec> spec = paginator.createMessage();

			return CommandResponse.create(spec, paginator);

		} else {
			return CommandResponse.create("The queue is empty");
		}
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Displays all the songs currently in the queue.");
	}

}
