package com.github.MudPitBot.music;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import reactor.util.Logger;
import reactor.util.Loggers;

public final class TrackScheduler extends AudioEventAdapter {

	private static final Logger LOGGER = Loggers.getLogger(TrackScheduler.class);

	// Queue of songs for this scheduler
	private BlockingQueue<AudioTrack> queue = new LinkedBlockingQueue<AudioTrack>();
	private final AudioPlayer player;

	/**
	 * Creates a track scheduler for the given channel
	 * 
	 * @param guildId the channel to create a track scheduler for
	 */
	public TrackScheduler(long guildId, AudioPlayer player) {
		this.player = player;
		// add this as a listener so we can listen for tracks ending
		player.addListener(this);
	}

	/**
	 * Adds a track to the queue to be played
	 * 
	 * @param track the track to queue
	 * @return
	 */
	public String queue(AudioTrack track) {
		// Calling startTrack with the noInterrupt set to true will start the track only
		// if nothing is currently playing. If
		// something is playing, it returns false and does nothing. In that case the
		// player was already playing so this
		// track goes to the queue instead.
		if (!player.startTrack(track, true)) {
			queue.offer(track);
			LOGGER.info("Track added to the queue: " + queue.size());
			return "New track added to the queue (#" + getQueue().size() + ")";
		}
		return "";
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
	 * Removes a specific track from the queue given by the index
	 * 
	 * @param index index of the item to remove from the queue
	 * @return the AudioTrack that was removed or null if none was removed
	 */
	public AudioTrack removeFromQueue(int index) {
		if (index >= queue.size() || index < 0)
			return null;

		// convert the queue into a list
		List<AudioTrack> listQueue = getQueue();
		// remove the item from that list
		AudioTrack removed = listQueue.remove(index);
		// convert the list back into a queue
		queue = new LinkedBlockingQueue<AudioTrack>(listQueue);

		return removed;
	}

	/**
	 * Gets a list of the songs that are currently in the queue.
	 * 
	 * @return List of queued songs
	 */
	public List<AudioTrack> getQueue() {
		List<AudioTrack> ret = new ArrayList<AudioTrack>(queue);
		return ret;
	}

	/**
	 * Shuffles the songs currently in the queue
	 */
	public void shuffleQueue() {
		// convert the queue to a list
		List<AudioTrack> ret = getQueue();
		// shuffle that list
		Collections.shuffle(ret);
		// convert the list back into a queue
		queue = new LinkedBlockingQueue<AudioTrack>(ret);
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
				if (currentTrack.isSeekable()) {
					long position = TimeUnit.SECONDS.toMillis(positionInSeconds);
					currentTrack.setPosition(position);
				}
			}
		}
	}

	/**
	 * Rewinds the currently playing track by the given amount of seconds
	 * 
	 * @param amountInSeconds Amount of time in seconds to rewind
	 */
	public void rewind(int amountInSeconds) {
		if (player != null) {
			AudioTrack currentTrack = player.getPlayingTrack();
			if (currentTrack != null) {
				if (currentTrack.isSeekable()) {
					long amountToRewind = TimeUnit.SECONDS.toMillis(amountInSeconds);
					long currentPosition = currentTrack.getPosition();

					long newPosition = currentPosition - amountToRewind;

					currentTrack.setPosition(newPosition);
				}
			}
		}
	}

	/**
	 * Fast forwards the currently playing track by the given amount of seconds
	 * 
	 * @param amountInSeconds Amount of time in seconds to fast forward
	 */
	public void fastForward(int amountInSeconds) {
		if (player != null) {
			AudioTrack currentTrack = player.getPlayingTrack();
			if (currentTrack != null) {
				if (currentTrack.isSeekable()) {
					long amountToFastforward = TimeUnit.SECONDS.toMillis(amountInSeconds);
					long currentPosition = currentTrack.getPosition();

					long newPosition = currentPosition + amountToFastforward;

					currentTrack.setPosition(newPosition);
				}
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

		LOGGER.info("TRACK ENDED");
		if (endReason != AudioTrackEndReason.STOPPED && endReason.mayStartNext
				&& track.getInfo().length == Long.MAX_VALUE) {
			// Live stream somehow ended, restart it again
			LOGGER.error("Live stream track ended, restarting track");
			player.startTrack(track.makeClone(), false);
			return;
		}

		// Only start the next track if the end reason is suitable for it (FINISHED or
		// LOAD_FAILED)
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

}