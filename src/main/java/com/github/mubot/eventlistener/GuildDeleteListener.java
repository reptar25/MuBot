package com.github.mubot.eventlistener;

import com.github.mubot.database.DatabaseManager;

import discord4j.core.event.domain.guild.GuildDeleteEvent;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class GuildDeleteListener implements EventListener<GuildDeleteEvent> {

	private static final Logger LOGGER = Loggers.getLogger(GuildDeleteListener.class);

	@Override
	public Class<GuildDeleteEvent> getEventType() {
		return GuildDeleteEvent.class;
	}

	@Override
	public Mono<Void> consume(GuildDeleteEvent e) {
		return Mono.just(e).flatMap(event -> {
			LOGGER.info("GuildDeleteEvent consumed: " + event.getGuildId().asLong());
			return DatabaseManager.getInstance().getGuildCache().removeGuild(event.getGuildId().asLong());
		});
	}

}
