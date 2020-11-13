package com.github.MudPitBot.main;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;

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
