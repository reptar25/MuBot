package com.github.MudPitBot.main;

import com.github.MudPitBot.botCommand.CommandClient;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;

public class Main {

	public static void main(String[] args) {
		String token = args[0];
		if (token == null)
			token = System.getenv("token");

		final GatewayDiscordClient client = DiscordClientBuilder.create(token).build().login().block();

		CommandClient.create(client);

		client.onDisconnect().block();
	}

}
