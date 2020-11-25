package com.github.MudPitBot.botCommand.sound;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;

public final class PlayerManager {

	private static AudioPlayerManager playerManager;

	private PlayerManager() {
	}

	static {
		// Creates AudioPlayer instances and translates URLs to AudioTrack instances
		playerManager = new DefaultAudioPlayerManager();
		// This is an optimization strategy that Discord4J can utilize. It is not
		// important to understand
		playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
		// Allow playerManager to parse remote sources like YouTube links
		AudioSourceManagers.registerRemoteSources(playerManager);
	}

	public static void loadItem(String identifier, TrackScheduler resultHandler) {
		playerManager.loadItem(identifier, resultHandler);
	}

	public static AudioPlayer createPlayer() {
		return playerManager.createPlayer();
	}

}
