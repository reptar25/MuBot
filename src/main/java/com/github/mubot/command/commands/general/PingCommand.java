package com.github.mubot.command.commands.general;

import java.time.Duration;
import java.util.function.Consumer;

import com.github.mubot.command.Command;
import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.help.CommandHelpSpec;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.gateway.GatewayClient;
import reactor.core.publisher.Mono;

import static com.github.mubot.command.util.PermissionsHelper.requireNotPrivateMessage;

public class PingCommand extends Command {

	public PingCommand() {
		super("ping");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireNotPrivateMessage(event).flatMap(ignored -> ping(event));
	}

	private Mono<CommandResponse> ping(MessageCreateEvent event) {
		String pingTime = event.getClient().getGatewayClientGroup().find(event.getShardInfo().getIndex())
				.map(GatewayClient::getResponseTime).map(Duration::toMillis).orElse(-1L).toString();
		String responseTime = String.format("Response time: %sms", pingTime);
		return CommandResponse.create(responseTime);
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Display the bot's current response time to the Discord API.");
	}

}
