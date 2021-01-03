package com.github.MudPitBot.music;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.github.MudPitBot.command.util.SoundCloudHtmlLoader;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

/**
 * Manages all of the TrackSchedulers for each guild
 */
public class GuildMusicManager {

	// private static final Logger LOGGER =
	// Loggers.getLogger(GuildMusicManager.class);
	private static AudioPlayerManager playerManager;

	public static final int DEFAULT_VOLUME = 15;
	private static GuildMusicManager instance;

	/**
	 * Maps a GuildMusic object for each new guild joined. Key is guild id snowflake
	 */
	private static Map<Snowflake, GuildMusic> guildMusicMap = new ConcurrentHashMap<>();

	static {
		GuildMusicManager.instance = new GuildMusicManager();
	}

	private GuildMusicManager() {
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

	public static Mono<GuildMusic> getOrCreate(Snowflake guildId) {
		return Mono.justOrEmpty(getGuildMusic(guildId)).switchIfEmpty(Mono.defer(() -> {
			final AudioPlayer player = playerManager.createPlayer();
			final LavaPlayerAudioProvider audioProvider = new LavaPlayerAudioProvider(player);
			final TrackScheduler scheduler = new TrackScheduler(player);
			final GuildMusic guildMusic = new GuildMusic(guildId, scheduler, audioProvider);

			player.setVolume(DEFAULT_VOLUME);
			guildMusicMap.put(guildId, guildMusic);

			return Mono.just(guildMusic);
		}));
	}

	public static void loadItemOrdered(String identifier, TrackScheduler scheduler, MessageCreateEvent event) {
		playerManager.loadItemOrdered(event.getGuildId().get(), identifier,
				new TrackLoadResultHandler(scheduler, event));
	}

	public static AudioPlayerManager getPlayerManager() {
		return playerManager;
	}

	public static Optional<GuildMusic> getGuildMusic(Snowflake guildId) {
		return Optional.ofNullable(guildMusicMap.get(guildId));
	}

	public static GuildMusicManager getInstance() {
		return instance;
	}

	public void destroy(Snowflake guildId) {
		final GuildMusic guildMusic = guildMusicMap.remove(guildId);
		if (guildMusic != null)
			guildMusic.destroy();
	}

}
