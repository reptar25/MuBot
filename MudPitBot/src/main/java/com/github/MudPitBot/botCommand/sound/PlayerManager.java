package com.github.MudPitBot.botCommand.sound;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;

import discord4j.voice.AudioProvider;

public class PlayerManager {

	public AudioProvider provider;
	public AudioPlayerManager playerManager;
	private AudioPlayer player;

	public static final int DEFAULT_VOLUME = 10;

	public PlayerManager() {
		// Creates AudioPlayer instances and translates URLs to AudioTrack instances
		playerManager = new DefaultAudioPlayerManager();
		// This is an optimization strategy that Discord4J can utilize. It is not
		// important to understand
		playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
		// Allow playerManager to parse remote sources like YouTube links
		AudioSourceManagers.registerRemoteSources(playerManager);
		// Create an AudioPlayer so Discord4J can receive audio data
		player = playerManager.createPlayer();
		player.setVolume(DEFAULT_VOLUME);
		// We will be creating LavaPlayerAudioProvider in the next step
		provider = new LavaPlayerAudioProvider(player);
	}

	public AudioPlayer getPlayer() {
		return player;
	}

//	static {
//		// Creates AudioPlayer instances and translates URLs to AudioTrack instances
//		playerManager = new DefaultAudioPlayerManager();
//		// This is an optimization strategy that Discord4J can utilize. It is not
//		// important to understand
//		playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
//		// Allow playerManager to parse remote sources like YouTube links
//		AudioSourceManagers.registerRemoteSources(playerManager);
//		// Create an AudioPlayer so Discord4J can receive audio data
//		player = playerManager.createPlayer();
//		player.setVolume(DEFAULT_VOLUME);
//		// We will be creating LavaPlayerAudioProvider in the next step
//		provider = new LavaPlayerAudioProvider(player);
//	}

}
