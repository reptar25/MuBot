package mubot.music;

import mubot.command.CommandResponse;
import mubot.command.util.CommandUtil;
import mubot.command.util.EmojiHelper;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class TrackLoadResultHandler implements AudioLoadResultHandler {

	private static final Logger LOGGER = Loggers.getLogger(TrackLoadResultHandler.class);
	private final TrackScheduler scheduler;
	private final MessageCreateEvent event;

	public TrackLoadResultHandler(TrackScheduler scheduler, MessageCreateEvent event) {
		this.scheduler = scheduler;
		this.event = event;
	}

	@Override
	public void trackLoaded(final AudioTrack track) {
		// LavaPlayer found an audio source for us to play
		LOGGER.info("Track loaded");
		String queueResponse = scheduler.queue(track);
		if (!queueResponse.isEmpty())
			CommandUtil.sendReply(event, CommandResponse.createFlat(queueResponse)).subscribe();
	}

	@Override
	public void playlistLoaded(final AudioPlaylist playlist) {
		// LavaPlayer found multiple AudioTracks from some playlist
		LOGGER.info("Playlist loaded");
		Mono.just(playlist).map(AudioPlaylist::getTracks);
		CommandUtil
				.sendReply(event, CommandResponse.createFlat(EmojiHelper.CHECK_MARK + " Playlist with "
						+ EmojiHelper.numToEmoji(playlist.getTracks().size()) + " songs added to queue " + EmojiHelper.CHECK_MARK))
				.subscribe();

		for (AudioTrack track : playlist.getTracks()) {
			scheduler.queue(track);
		}
	}

	@Override
	public void noMatches() {
		// LavaPlayer did not find any audio to extract
		LOGGER.info("Did not find any audio to extract");
		CommandUtil
				.sendReply(event,
						CommandResponse.createFlat(EmojiHelper.NO_ENTRY
								+ " Problem loading track, did not find any audio to extract " + EmojiHelper.NO_ENTRY))
				.subscribe();
	}

	@Override
	public void loadFailed(final FriendlyException exception) {
		// LavaPlayer could not parse an audio source for some reason
		LOGGER.error("Error loading audio track: " + exception.getMessage());
		CommandUtil
				.sendReply(event,
						CommandResponse.createFlat(
								EmojiHelper.NO_ENTRY + " Could not parse this track for some reason " + EmojiHelper.NO_ENTRY))
				.subscribe();
	}

}
