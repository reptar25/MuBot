package com.github.mubot.eventlistener;

import com.github.mubot.music.GuildMusicManager;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class VoiceStateUpdateListener implements EventListener<VoiceStateUpdateEvent> {

	private static final Logger LOGGER = Loggers.getLogger(VoiceStateUpdateListener.class);

	@Override
	public Class<VoiceStateUpdateEvent> getEventType() {
		return VoiceStateUpdateEvent.class;
	}

	@Override
	public Mono<Void> consume(VoiceStateUpdateEvent e) {
		// If the voice state update comes from the bot...
		return Mono.just(e).filter(event -> event.getCurrent().getUserId().equals(event.getClient().getSelfId()))
				.flatMap(VoiceStateUpdateListener::processBotVoiceStateUpdateEvent).onErrorResume(error -> {
					LOGGER.error("Error processing bot VoiceStateUpdateEvent.", error);
					return Mono.empty();
				});
	}

	private static Mono<Void> processBotVoiceStateUpdateEvent(VoiceStateUpdateEvent event) {
		final Snowflake guildId = event.getCurrent().getGuildId();
		if (event.isLeaveEvent()) {
			LOGGER.info("{Guild ID: {}} Voice channel left {}", guildId.asLong(), event);
			GuildMusicManager.destroy(guildId);
		} else if (event.isJoinEvent()) {
			LOGGER.info("{Guild ID: {}} Voice channel joined", guildId.asLong());
		} else if (event.isMoveEvent()) {
			LOGGER.info("{Guild ID: {}} Voice channel moved", guildId.asLong());

		}

		return Mono.empty();
	}

}
