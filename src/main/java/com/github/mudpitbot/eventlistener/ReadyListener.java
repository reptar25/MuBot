package com.github.mudpitbot.eventlistener;

import java.time.Duration;

import com.github.mudpitbot.music.GuildMusicManager;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import reactor.core.publisher.Flux;
import reactor.function.TupleUtils;
import reactor.util.Logger;
import reactor.util.Loggers;

public class ReadyListener {

	private static final Logger LOGGER = Loggers.getLogger(ReadyListener.class);
	private GatewayDiscordClient client;
	private static ReadyListener instance;

	private ReadyListener(GatewayDiscordClient client) {
		this.client = client;

		setupListener();
	}

	public static ReadyListener create(GatewayDiscordClient client) {
		if (instance == null)
			instance = new ReadyListener(client);

		return instance;
	}

	private void setupListener() {
		if (client.getEventDispatcher() != null) {

			// process ReadyEvent and reconnect to any voice channels the bot is still in.
			// Only listeners for the first 2 minutes of the bot starting
			client.getEventDispatcher().on(ReadyEvent.class).take(Duration.ofMinutes(2)).doOnTerminate(() -> dispose())
					.flatMap(ReadyListener::processReadyEvent)
					.subscribe(null, error -> LOGGER.error(error.getMessage(), error));
		}
	}

	/**
	 * Called once initial handshakes with gateway are completed. Will reconnect any
	 * voice channels the bot is still connected to and create a TrackScheduler
	 * 
	 * @param event the {@link ReadyEvent}
	 * @return
	 */
	private static Flux<Void> processReadyEvent(ReadyEvent event) {
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

	private void dispose() {
		instance = null;
		client = null;
	}

}
