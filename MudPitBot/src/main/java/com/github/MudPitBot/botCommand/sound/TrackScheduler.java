package com.github.MudPitBot.botCommand.sound;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import reactor.util.Logger;
import reactor.util.Loggers;

public final class TrackScheduler extends AudioEventAdapter implements AudioLoadResultHandler {

	private static final Logger LOGGER = Loggers.getLogger(TrackScheduler.class);

	public static BlockingQueue<AudioTrack> queue;

	private final AudioPlayer player;

	public TrackScheduler(final AudioPlayer player) {
		this.player = player;

		TrackScheduler.queue = new LinkedBlockingQueue<>();
		
		// add this as a listener so we can listen for tracks ending
		player.addListener(this);
	}
	
	  public void queue(AudioTrack track) {
		    // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
		    // something is playing, it returns false and does nothing. In that case the player was already playing so this
		    // track goes to the queue instead.
		    if (!player.startTrack(track, true)) {
		      queue.offer(track);
		      LOGGER.info("Track added to the queue: "+queue.size());
		    }
		  }

	@Override
	public void trackLoaded(final AudioTrack track) {
		// LavaPlayer found an audio source for us to play
		//if (player.getPlayingTrack() != null)
		//	player.playTrack(track);
		//else {
		queue(track);
		//}
	}

	@Override
	public void playlistLoaded(final AudioPlaylist playlist) {
		// LavaPlayer found multiple AudioTracks from some playlist
		LOGGER.info("Playlist loaded");

		for (AudioTrack track : playlist.getTracks()) {
			queue(track);
		}
	}

	@Override
	public void noMatches() {
		// LavaPlayer did not find any audio to extract
		LOGGER.info("Did not find any audio to extract");
	}

	@Override
	public void loadFailed(final FriendlyException exception) {
		// LavaPlayer could not parse an audio source for some reason
		LOGGER.info("Could not parse an audio source for some reason");
	}

	/**
	 * Start the next track, stopping the current one if it is playing.
	 */
	public void nextTrack() {
		// Start the next track, regardless of if something is already playing or not.
		// In case queue was empty, we are
		// giving null to startTrack, which is a valid argument and will simply stop the
		// player.
		player.startTrack(queue.poll(), false);
	}
	
	/*
	 * Clears the queue of all objects
	 */
	public void clearQueue() {
		queue.clear();
	}

	/*
	 * Called when the current track ends
	 */
	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		// Only start the next track if the end reason is suitable for it (FINISHED or
		// LOAD_FAILED)
		LOGGER.info("TRACK ENDED");
		if (endReason.mayStartNext) {
			nextTrack();
		}
	}
}