package com.github.MudPitBot.main;

import com.github.MudPitBot.botCommand.BotClient;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;

public class Main {

	/*
	 * TODO: Create bot on https://discord.com/developers/applications/ to get a
	 * TokenKey to login to discord with.
	 */

	public static void main(String[] args) {
		final GatewayDiscordClient client = DiscordClientBuilder.create(args[0]).build().login().block();

		BotClient.create(client);
		
		client.onDisconnect().block();
	}

}
