package com.github.MudPitBot.botCommand;

import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import com.github.MudPitBot.botCommand.commandInterface.Command;
import com.github.MudPitBot.botCommand.commandInterface.Commands;
import com.github.MudPitBot.botCommand.poll.Poll;
import com.github.MudPitBot.botCommand.sound.PlayerManager;
import com.github.MudPitBot.botCommand.sound.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.rest.util.Color;
import discord4j.voice.VoiceConnection;
import reactor.util.Logger;
import reactor.util.Loggers;

/*
* A receiver class for command pattern. A receiver is an object that performs a set of cohesive actions. 
* It's the component that performs the actual action when the command's execute() method is called.
* https://www.baeldung.com/java-command-pattern
*/
public class CommandReceiver {

	private static final Logger LOGGER = Loggers.getLogger(CommandReceiver.class);
	private static CommandReceiver instance;
	private static Random rand = new Random();
	private static TrackScheduler scheduler;

	public static boolean muteToggle = false;
	public static long muteChannelId = 0;

	private static VoiceConnection currentVoiceConnection;

	public static CommandReceiver getInstance() {
		if (instance == null)
			instance = new CommandReceiver();
		return instance;
	}

	private CommandReceiver() {
		scheduler = new TrackScheduler(PlayerManager.player);
	}

	/*
	 * Bot joins the same voice channel as the user who uses the command.
	 */
	public String join(MessageCreateEvent event) {
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
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									LOGGER.error(e.toString());
								}
							}
							// join returns a VoiceConnection which would be required if we were
							// adding disconnection features, but for now we are just ignoring it.
							currentVoiceConnection = channel.join(spec -> spec.setProvider(PlayerManager.provider))
									.block();
						}
					}
				}
			}
		}

		return null;
	}

	/*
	 * Bot leaves any voice channel it has previously joined into using the join
	 * command
	 */
	public String leave() {
		if (currentVoiceConnection != null) {
			currentVoiceConnection.disconnect().block();
			LOGGER.info("Discconecting from channel");
		}

		return null;
	}

	/*
	 * Bot replies with a simple echo message
	 */
	public String echo() {
		return ("echo!");
	}

	/*
	 * Bot rolls dice and displays results
	 */
	public String roll(String[] params) {

		if (params == null) {
			return null;
		}
		// will be the 2nd part of command eg "1d20"
		if (params.length <= 0) {
			return null;
		}

		String dice = params[0];

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
			return sb.toString();
			// channel to display the results in
//						MessageChannel channel = event.getMessage().getChannel().block();
//						if (channel != null)
//							channel.createMessage(sb.toString()).block();
		}

		return null;
	}

	/*
	 * Attempts to play the link in the message
	 */
	public String play(String[] params) {

		if (params != null) {

			// unpause
			if (params[0].isEmpty() && scheduler.isPaused()) {
				scheduler.pause(false);
				return null;
			}

			if (params.length <= 0 || params.length > 1 || params[0].isEmpty()) {
				LOGGER.error("Too many or few params for play");
				return null;
			}
			PlayerManager.playerManager.loadItem(params[0], scheduler);
			LOGGER.info("Loaded music item: " + params[0]);
		}

		return null;
	}

	/*
	 * Sets the volume of the LavaPlayer
	 */
	public String volume(String[] params) {
		if (params != null) {
			// final String content = event.getMessage().getContent();
			// final String[] command = content.split(" ");
			if (params.length <= 0 || params.length > 1) {
				LOGGER.error("Too many or few params for volume");
			}

			if (Pattern.matches("[1-9]*[0-9]*[0-9]", params[0])) {
				int volume = Integer.parseInt(params[0]);
				PlayerManager.player.setVolume(volume);
				StringBuilder sb = new StringBuilder("Set volume to ").append(volume);
				return sb.toString();
//					LOGGER.info(sb.toString());
//					MessageChannel channel = event.getMessage().getChannel().block();
//					if (channel != null)
//						channel.createMessage(sb.toString()).block();
			}
		}
		return null;
	}

	/*
	 * Stops the LavaPlayer if it is playing anything
	 */
	public String stop() {
		if (PlayerManager.player != null) {
			PlayerManager.player.stopTrack();
			LOGGER.info("Stopped music");
		}

		return null;
	}

	/*
	 * Stops the current song and plays the next in queue if there is any
	 */
	public String skip() {
		if (scheduler != null)
			scheduler.nextTrack();

		return null;
	}

	/*
	 * Mutes all {@link Member} in the channel besides bots and itself
	 */

	public String mute(MessageCreateEvent event) {
		if (event != null && event.getMessage() != null && event.getMember().isPresent()) {
			muteToggle = !muteToggle;

//				if (event.getMember().orElse(null).getVoiceState().block().getChannel().block().getId()
//						.asLong() != botChannelId) {
//					return;
//				}
			// gets the member's channel who sent the message, and then all the VoiceStates
			// connected to that channel. From there we can get the Member of the VoiceState
			List<VoiceState> users = event.getMember().orElse(null).getVoiceState().block().getChannel().block()
					.getVoiceStates().collectList().block();
			if (users != null) {
				muteChannelId = event.getMember().orElse(null).getVoiceState().block().getChannel().block().getId()
						.asLong();
				for (VoiceState user : users) {

					// don't mute itself or other bots
					if (user.getMember().block().isBot())
						continue;

					LOGGER.info("Muting user " + user.getUser().block().getUsername());
					// mute all users
					user.getMember().block().edit(spec -> spec.setMute(muteToggle)).block();
				}
			}
		}

		return null;
	}

	/*
	 * Clears the current queue of all objects
	 */
	public String clearQueue() {
		if (scheduler != null)
			scheduler.clearQueue();

		return null;
	}

	/*
	 * Prints out a list of the currently queued songs
	 */
	public String viewQueue() {
		// get list of songs currently in the queue
		List<AudioTrack> queue = scheduler.getQueue();
		StringBuilder sb = new StringBuilder();
		// if the queue is not empty
		if (queue.size() > 0) {
			// print total number of songs
			sb.append("Number of songs in queue: ").append(queue.size()).append("\n");
			for (AudioTrack track : queue) {
				// print title and author of song on its own line
				sb.append("\"").append(track.getInfo().title).append("\"").append(" by ").append(track.getInfo().author)
						.append("\n");
			}
		} else {
			sb.append("The queue is empty.");
		}

		String retString = sb.toString();

		// if the message is longer than 2000 character, trim it so that its not over
		// the max character limit.
		if (sb.toString().length() >= Message.MAX_CONTENT_LENGTH)
			retString = sb.substring(0, Message.MAX_CONTENT_LENGTH - 1);

		return retString;
	}

	/*
	 * Print out the info for the currently playing song
	 */
	public String nowPlaying() {
		StringBuilder sb = new StringBuilder("Now playing: ");
		// get the track that's currently playing
		AudioTrack track = scheduler.getNowPlaying();
		if (track != null) {
			// add track title and author
			sb.append("\"").append(track.getInfo().title).append("\"").append(" by ").append(track.getInfo().author);
		}

		return sb.toString();
	}

	/*
	 * Creates a poll in the channel
	 */
	public String poll(MessageCreateEvent event) {
		if (event != null) {
			if (event.getMessage() != null) {
				MessageChannel channel = event.getMessage().getChannel().block();
				if (channel != null) {
					// create a new poll object
					Poll poll = new Poll.Builder(event).build();

					// if the poll is invalid just stop
					if (poll.getAnswers().size() <= 1) {
						return null;
					}

					// create the embed to put the poll into
					Message message = channel.createEmbed(
							spec -> spec.setColor(Color.of(23, 53, 77)).setFooter(poll.getFooter(), poll.getFooterURL())
									.setTitle(poll.getTitle()).setDescription(poll.getDescription()))
							.block();

					if (message != null) {
						// add reactions as vote tickers, number of reactions depends on number of
						// answers
						poll.addReactions(message);
						// message.pin().block();
					}
				}
			}
		}

		return null;
	}

	/*
	 * Pauses/unpauses the player
	 */
	public String pause() {
		scheduler.pause(!scheduler.isPaused());

		return null;
	}

	/**
	 * Prints a list of all commands in the channel the message was sent.
	 * @return The message to respond with
	 */
	public String printCommands() {

		StringBuilder sb = new StringBuilder("Available commands:");
		Set<Entry<String, Command>> entries = Commands.getEntries();
		for (Entry<String, Command> entry : entries) {
			sb.append(", ").append(Commands.COMMAND_PREFIX).append(entry.getKey());
		}
		return sb.toString().replaceAll(":,", ":");

	}
}
