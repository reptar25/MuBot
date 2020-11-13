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

	public static void main(String[] args) {

		String token = System.getenv("token");
		if (token == null)
			token = args[0];
		final GatewayDiscordClient client = DiscordClientBuilder.create(token).build().login().block();

		BotClient.create(client);

		client.onDisconnect().block();
	}

}
