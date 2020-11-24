package com.github.MudPitBot.botCommand.sound;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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

	private BlockingQueue<AudioTrack> queue = new LinkedBlockingQueue<>();

	private final PlayerManager playerManager;
	private final AudioPlayer player;

	public TrackScheduler(final PlayerManager playerManager) {
		this.playerManager = playerManager;
		this.player = playerManager.getPlayer();
		// add this as a listener so we can listen for tracks ending
		player.addListener(this);
	}

	public void queue(AudioTrack track) {
		// Calling startTrack with the noInterrupt set to true will start the track only
		// if nothing is currently playing. If
		// something is playing, it returns false and does nothing. In that case the
		// player was already playing so this
		// track goes to the queue instead.
		if (!player.startTrack(track, true)) {
			queue.offer(track);
			LOGGER.info("Track added to the queue: " + queue.size());
		}
	}

	@Override
	public void trackLoaded(final AudioTrack track) {
		// LavaPlayer found an audio source for us to play
		// if (player.getPlayingTrack() != null)
		// player.playTrack(track);
		// else {
		queue(track);
		// }
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

	/**
	 * Clears the queue of all objects
	 */
	public void clearQueue() {
		queue.clear();
	}

	/**
	 * Gets a list of the songs that are currently in the queue.
	 * 
	 * @return List of queued songs
	 */
	public List<AudioTrack> getQueue() {
		List<AudioTrack> ret = new ArrayList<AudioTrack>();
		Object[] queueArr = queue.toArray();
		for (int i = 0; i < queueArr.length; i++) {
			if (queueArr[i] instanceof AudioTrack) {
				AudioTrack track = (AudioTrack) queueArr[i];
				ret.add(track);
			}

		}
		return ret;
	}

	/**
	 * @return true if player is currently paused, false otherwise
	 */
	public boolean isPaused() {

		if (player == null)
			return false;

		return player.isPaused();
	}

	/**
	 * @param pause sets if the player is paused or not
	 */
	public void pause(boolean pause) {
		if (player != null)
			player.setPaused(pause);
	}

	/**
	 * Sets the position of the currently playing track to the given time.
	 * 
	 * @param positionInSeconds Position to set the track to in seconds
	 */
	public void seek(int positionInSeconds) {
		if (player != null) {
			AudioTrack currentTrack = player.getPlayingTrack();
			if (currentTrack != null) {
				long position = TimeUnit.SECONDS.toMillis(positionInSeconds);
				currentTrack.setPosition(position);
			}
		}
	}

	/**
	 * Gets the track that is currently playing.
	 * 
	 * @return the AudioTrack that is playing
	 */
	public AudioTrack getNowPlaying() {
		return player.getPlayingTrack();
	}

	/**
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

	/**
	 * @return The {@link AudioPlayer} for this {@link TrackScheduler}
	 */
	public AudioPlayer getPlayer() {
		return player;
	}

	/**
	 * @return The {@link PlayerManager} for this {@link TrackScheduler}
	 */
	public PlayerManager getPlayerManager() {
		return playerManager;
	}
}