package com.github.MudPitBot.main;

import com.github.MudPitBot.botCommand.sound.LavaPlayerAudioProvider;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.voice.AudioProvider;

public class Main {

	public static AudioProvider provider;
	public static AudioPlayerManager playerManager;
	public static AudioPlayer player;

	public static void main(String[] args) {

		// Creates AudioPlayer instances and translates URLs to AudioTrack instances
		playerManager = new DefaultAudioPlayerManager();
		// This is an optimization strategy that Discord4J can utilize. It is not
		// important to understand
		playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
		// Allow playerManager to parse remote sources like YouTube links
		AudioSourceManagers.registerRemoteSources(playerManager);
		// Create an AudioPlayer so Discord4J can receive audio data
		player = playerManager.createPlayer();
		player.setVolume(1);
		// We will be creating LavaPlayerAudioProvider in the next step
		provider = new LavaPlayerAudioProvider(player);

		String token = System.getenv("token");
		if (token == null)
			token = args[0];
		final GatewayDiscordClient client = DiscordClientBuilder.create(token).build().login().block();

		BotClient.create(client);

		client.onDisconnect().block();
	}

}
