package com.github.MudPitBot.botCommand;

import java.util.Random;
import java.util.regex.Pattern;

import com.github.MudPitBot.botCommand.sound.LavaPlayerAudioProvider;
import com.github.MudPitBot.botCommand.sound.PlayerManager;
import com.github.MudPitBot.botCommand.sound.TrackScheduler;
import com.github.MudPitBot.main.Main;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.VoiceConnection;
import reactor.util.Logger;
import reactor.util.Loggers;

/*
* A receiver class for command pattern. A receiver is an object that performs a set of cohesive actions. 
* It's the component that performs the actual action when the command's execute() method is called.
* https://www.baeldung.com/java-command-pattern
*/
public class BotReceiver {

	private static final Logger LOGGER = Loggers.getLogger(BotReceiver.class);
	private static BotReceiver instance;
	private static Random rand = new Random();
	private static TrackScheduler scheduler;

	public static BotReceiver getInstance() {
		if (instance == null)
			instance = new BotReceiver();
		return instance;
	}

	private BotReceiver() {
		scheduler = new TrackScheduler(PlayerManager.player);
	}

	/*
	 * Bot joins the same voice channel as the user who uses the command.
	 */
	public void join(MessageCreateEvent event) {
		if (event != null) {
			if (event.getMember() != null) {
				// get member who used command
				final Member member = event.getMember().orElse(null);
				if (member != null) {
					// get voice channel member is in
					final VoiceState voiceState = member.getVoiceState().block();
					if (voiceState != null) {
						final VoiceChannel channel = voiceState.getChannel().block();
						if (channel != null) {
							// check if bot is currently connected to another voice channel and disconnect
							// from it before trying to join a new one.
							if (event.getMessage().getGuild().block().getVoiceConnection().block() != null) {
								event.getMessage().getGuild().block().getVoiceConnection().block().disconnect().block();
							}
							// join returns a VoiceConnection which would be required if we were
							// adding disconnection features, but for now we are just ignoring it.
							channel.join(spec -> spec.setProvider(PlayerManager.provider)).block();//

						}
					}
				}
			}
		}
	}

	/*
	 * Bot leaves the voice channel if its the same as the one the user is connected
	 * to.
	 */
	public void leave(MessageCreateEvent event) {
		if (event != null) {
			if (event.getMessage() != null) {
				Guild guild = event.getMessage().getGuild().block();
				VoiceConnection botConnection = guild.getVoiceConnection().block();
				// If the client isn't in a voiceChannel, don't execute any other code
				if (botConnection == null) {
					// System.out.println("BOT NOT IN A VOICE CHANNEL");
					return;
				}
				// get member who used command
				final Member member = event.getMember().orElse(null);
				if (member != null) {
					// get voice channel member is in
					final VoiceState voiceState = member.getVoiceState().block();
					if (voiceState != null) {
						long botChannelId = botConnection.getChannelId().block().asLong();
						long memberChannelId = voiceState.getChannel().block().getId().asLong();
						// check if user and bot are in the same channel
						if (memberChannelId == botChannelId) {
							botConnection.disconnect().block();
							LOGGER.info("Bot disconnecting from voice channel.");
							// System.out.println("DISCONNECTING");
						}
					}
				}
			}
		}
	}

	/*
	 * Bot replies with a simple echo message
	 */
	public void echo(MessageCreateEvent event) {
		if (event != null) {
			if (event.getMessage() != null) {
				event.getMessage().getChannel().block().createMessage("echo!").block();
			}
		}
	}

	/*
	 * Bot rolls dice and displays results
	 */
	public void roll(MessageCreateEvent event) {

		if (event != null) {
			if (event.getMessage() != null) {
				if (event.getMessage().getContent() != null) {
					// will be the 2nd part of command eg "1d20"
					String[] splitString = event.getMessage().getContent().split(" ");
					if (splitString.length <= 1) {
						return;
					}

					String dice = splitString[1];

					// only roll if 2nd part of command matches the reg ex
					if (Pattern.matches("[1-9][0-9]*d[1-9][0-9]*", dice)) {
						LOGGER.info(("Regex matches"));

						StringBuilder sb = new StringBuilder();
						sb.append("Rolling " + dice + "\n");

						String[] splitDiceString = dice.split("d");
						int numOfDice = Integer.parseInt(splitDiceString[0]);
						int numOfSides = Integer.parseInt(splitDiceString[1]);
						int diceSum = 0;

						for (int i = 0; i < numOfDice; i++) {
							int roll = rand.nextInt(numOfSides) + 1;
							sb.append("Dice " + (i + 1) + " was a " + roll + "\n");
							diceSum += roll;
						}

						sb.append("Rolled a " + diceSum + "\n");
						// channel to display the results in
						MessageChannel channel = event.getMessage().getChannel().block();
						if (channel != null)
							channel.createMessage(sb.toString()).block();
					}
				}
			}
		}
	}

	/*
	 * Attempts to play the link in the message
	 */
	public void play(MessageCreateEvent event) {
		if (event != null) {
			if (event.getMessage() != null) {
				final String content = event.getMessage().getContent();
				final String[] command = content.split(" ");
				if (command.length <= 1 || command.length > 2) {
					return;
				}
				PlayerManager.playerManager.loadItem(command[1], scheduler);
				LOGGER.info("Loaded music item");
			}
		}
	}

	/*
	 * Sets the volume of the LavaPlayer
	 */
	public void volume(MessageCreateEvent event) {
		if (event != null) {
			if (event.getMessage() != null) {
				final String content = event.getMessage().getContent();
				final String[] command = content.split(" ");
				if (command.length <= 1 || command.length > 2) {
					return;
				}

				if (Pattern.matches("[1-9]*[0-9]*[0-9]", command[1])) {
					int volume = Integer.parseInt(command[1]);
					PlayerManager.player.setVolume(volume);
					LOGGER.info("Set volume to " + volume);
				}
			}
		}
	}

	/*
	 * Stops the LavaPlayer if it is playing anything
	 */
	public void stop(MessageCreateEvent event) {
		if (PlayerManager.player != null) {
			PlayerManager.player.stopTrack();
			LOGGER.info("Stopped music");
		}
	}

	/*
	 * Stops the current song and plays the next in queue if there is any
	 */
	public void skip(MessageCreateEvent event) {
		if (scheduler != null)
			scheduler.nextTrack();
	}
}
