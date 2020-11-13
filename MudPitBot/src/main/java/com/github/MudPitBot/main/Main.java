package com.github.MudPitBot.main;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;

public class Main {
	public static void main(String[] args) {
		final GatewayDiscordClient client = DiscordClientBuilder.create(args[0]).build().login().block();

		BotClient.create(client);
		
		client.onDisconnect().block();
	}

}
