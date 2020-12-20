package com.github.MudPitBot.music;

import java.util.HashMap;

import com.github.MudPitBot.command.util.SoundCloudHtmlLoader;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.util.Logger;
import reactor.util.Loggers;

/**
 * Manages all of the TrackSchedulers for each guild
 */
public class GuildMusicManager {

	private static final Logger LOGGER = Loggers.getLogger(GuildMusicManager.class);
	private static AudioPlayerManager playerManager;

	public static final int DEFAULT_VOLUME = 15;

	/**
	 * Maps a new TrackScheduler for each new guild joined. Key is guild id
	 * snowflake
	 */
	private static HashMap<Long, TrackScheduler> schedulerMap = new HashMap<Long, TrackScheduler>();

	static {
		// Creates AudioPlayer instances and translates URLs to AudioTrack instances
		playerManager = new DefaultAudioPlayerManager();
		playerManager.registerSourceManager(
				SoundCloudAudioSourceManager.builder().withHtmlDataLoader(new SoundCloudHtmlLoader()).build());
		// This is an optimization strategy that Discord4J can utilize. It is not
		// important to understand
		playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
		// Allow playerManager to parse remote sources like YouTube links
		AudioSourceManagers.registerRemoteSources(playerManager);
	}

	public static void loadItem(String identifier, TrackScheduler scheduler, MessageCreateEvent event) {
		playerManager.loadItemOrdered(event.getGuildId().get(), identifier,
				new TrackLoadResultHandler(scheduler, event));
	}

	public static TrackScheduler createTrackScheduler(long guildId) {
		AudioPlayer player = playerManager.createPlayer();
		player.setVolume(DEFAULT_VOLUME);
		TrackScheduler scheduler = new TrackScheduler(guildId, player);
		schedulerMap.put(guildId, scheduler);
		return scheduler;
	}

	/**
	 * Get the track scheduler for the guild of this event
	 * 
	 * @param event The message event
	 * @return The scheduler mapped to this channel
	 */
	public static TrackScheduler getScheduler(long guildId) {
		return schedulerMap.get(guildId);
	}

	/**
	 * Removes channel and destroys audio player if present in the map
	 * 
	 * @param guildId channel id of the channel to remove
	 */
	public static void removeFromMap(long guildId) {
		if (schedulerMap.containsKey(guildId)) {
			LOGGER.info("Removing TrackScheduler with id " + guildId);
			schedulerMap.get(guildId).getPlayer().destroy();
			schedulerMap.remove(guildId);
		} else {
			LOGGER.info("No TrackScheduler found with id " + guildId);
		}
	}

	public static boolean containsTrackScheduler(long guildId) {
		return schedulerMap.containsKey(guildId);
	}

	public static AudioPlayerManager getPlayerManager() {
		return playerManager;
	}

}
