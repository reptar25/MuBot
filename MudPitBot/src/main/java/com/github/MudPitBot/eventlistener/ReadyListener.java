package com.github.MudPitBot.eventlistener;

import java.time.Duration;

import com.github.MudPitBot.music.GuildMusicManager;
import com.github.MudPitBot.music.LavaPlayerAudioProvider;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.voice.VoiceConnection.State;
import reactor.core.publisher.Flux;
import reactor.function.TupleUtils;
import reactor.util.Logger;
import reactor.util.Loggers;

public class ReadyListener {

	private static final Logger LOGGER = Loggers.getLogger(ReadyListener.class);
	private GatewayDiscordClient client;
	// private static CommandExecutor executor = new CommandExecutor();
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
					.subscribe(event -> {
						processReadyEvent(event);
					});
		}
	}

	private void dispose() {
		instance = null;
		client = null;
	}

	/**
	 * Called once initial handshakes with gateway are completed. Will reconnect any
	 * voice channels the bot is still connected to and create a TrackScheduler
	 * 
	 * @param event the {@link ReadyEvent}
	 */
	private void processReadyEvent(ReadyEvent event) {
		Flux.fromIterable(event.getGuilds()).subscribe(guild -> {
			event.getSelf().asMember(guild.getId()).flatMap(Member::getVoiceState).flatMap(VoiceState::getChannel)
					.flatMap(channel -> {
						GuildMusicManager.createTrackScheduler(channel.getGuildId().asLong());
						return channel
								.join(spec -> spec.setProvider(new LavaPlayerAudioProvider(
										GuildMusicManager.getScheduler(channel.getGuildId().asLong()).getPlayer())))
								.doOnNext(vc -> {
									// subscribe to connected/disconnected events
									vc.onConnectOrDisconnect().subscribe(newState -> {
										if (newState.equals(State.CONNECTED)) {
											LOGGER.info("Bot connected to channel with id " + channel.getId().asLong());
										} else if (newState.equals(State.DISCONNECTED)) {
											// remove the scheduler from the map.
											// This doesn't ever seem to happen when the bot
											// disconnects, though, so also remove it from map
											// during leave command
											GuildMusicManager.removeFromMap(channel.getGuildId().asLong());
											LOGGER.info("Bot disconnected from channel with id "
													+ channel.getId().asLong());
										}
									});
								});
					}).elapsed()
					.doOnNext(TupleUtils.consumer(
							(elapsed, response) -> LOGGER.info("ReadyEvent took {} ms to be processed", elapsed)))
					.subscribe();
		});
	}

}
