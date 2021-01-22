package com.github.mubot.eventlistener;

import com.github.mubot.music.GuildMusicManager;

import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.Logger;
import reactor.util.Loggers;

public class ReadyListener implements EventListener<ReadyEvent> {

	private static final Logger LOGGER = Loggers.getLogger(ReadyListener.class);

	@Override
	public Class<ReadyEvent> getEventType() {
		return ReadyEvent.class;
	}

	@Override
	public Mono<Void> consume(ReadyEvent e) {
		return Mono.just(e).flatMapMany(ReadyListener::processReadyEvent).then();
	}

	/**
	 * Called once initial handshakes with gateway are completed. Will reconnect any
	 * voice channels the bot is still connected to and create a TrackScheduler
	 * 
	 * @param event the {@link ReadyEvent}
	 * @return
	 */
	private static Flux<Object> processReadyEvent(ReadyEvent event) {
		return Flux.fromIterable(event.getGuilds()).flatMap(guild -> {
			return event.getSelf().asMember(guild.getId()).flatMap(Member::getVoiceState)
					.flatMap(VoiceState::getChannel).flatMap(channel -> {
						return GuildMusicManager.getOrCreate(channel.getGuildId()).flatMap(
								guildMusic -> channel.join(spec -> spec.setProvider(guildMusic.getAudioProvider())));
					}).elapsed()
					.doOnNext(TupleUtils.consumer(
							(elapsed, response) -> LOGGER.info("ReadyEvent took {} ms to be processed", elapsed)))
					.then();
		});
	}
}
