package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.sound.LavaPlayerAudioProvider;
import com.github.MudPitBot.sound.TrackScheduler;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.voice.VoiceConnection;
import discord4j.voice.VoiceConnection.State;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class JoinVoiceCommand extends Command {

	private static final Logger LOGGER = Loggers.getLogger(JoinVoiceCommand.class);

	public JoinVoiceCommand() {
		super("join");
	};

	@Override
	public CommandResponse execute(MessageCreateEvent event, String[] params) {
		return join(event);
	}

	/**
	 * Bot joins the same voice channel as the user who uses the command.
	 * 
	 * @param event The message event
	 * @return null
	 */
	public CommandResponse join(MessageCreateEvent event) {
		Mono.just(event).flatMap(MessageCreateEvent::getGuild).flatMap(Guild::getVoiceConnection)
				.flatMap(VoiceConnection::getChannelId).defaultIfEmpty(Snowflake.of(-1)).flatMap(botChannelId -> {
					// get the voice channel of the member who sent the message
					return Mono.justOrEmpty(event.getMember()).flatMap(Member::getVoiceState)
							.flatMap(VoiceState::getChannel).flatMap(channel -> {
								return Mono.just(channel.getId())
										// dont join the channel if the bot is already connect to that channel
										.filter(channelId -> !channelId.equals(botChannelId)).flatMap(channelId -> {
											// joining a new channel
											// once we are connected put the scheduler in the map with the
											// channelId as the key
											TrackScheduler scheduler = new TrackScheduler(channelId);
											return channel
													.join(spec -> spec.setProvider(
															new LavaPlayerAudioProvider(scheduler.getPlayer())))
													.doOnNext(vc -> {
														// subscribe to connected/disconnected events
														vc.onConnectOrDisconnect().subscribe(newState -> {
															if (newState.equals(State.CONNECTED)) {
																LOGGER.info("Bot connected to channel");
															} else if (newState.equals(State.DISCONNECTED)) {
																// remove the scheduler from the map. This doesn't ever
																// seem to
																// happen when the bot disconnects, though, so also
																// remove it from
																// map during leave command
																TrackScheduler.remove(channelId);
																LOGGER.info("Bot disconnected to channel");
															}
														});
													});
										});
							});
				}).subscribe(null, error -> LOGGER.error(error.getMessage(), error));
		return null;
	}

}
