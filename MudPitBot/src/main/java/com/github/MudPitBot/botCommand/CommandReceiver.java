package com.github.MudPitBot.botCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import com.github.MudPitBot.botCommand.commandInterface.Command;
import com.github.MudPitBot.botCommand.commandInterface.Commands;
import com.github.MudPitBot.botCommand.poll.Poll;
import com.github.MudPitBot.botCommand.sound.LavaPlayerAudioProvider;
import com.github.MudPitBot.botCommand.sound.PlayerManager;
import com.github.MudPitBot.botCommand.sound.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.rest.util.Color;
import discord4j.voice.VoiceConnection;
import discord4j.voice.VoiceConnection.State;
import reactor.util.Logger;
import reactor.util.Loggers;

/**
 * A receiver class for command pattern. A receiver is an object that performs a
 * set of cohesive actions. It's the component that performs the actual action
 * when the command's execute() method is called.
 * https://www.baeldung.com/java-command-pattern
 */
public class CommandReceiver {

	private static final Logger LOGGER = Loggers.getLogger(CommandReceiver.class);
	private static CommandReceiver instance;
	private static Random rand = new Random();

	/**
	 * Maps a new TrackScheduler for each new voice channel joined. Key is channel
	 * id snowflake
	 */
	private static HashMap<Snowflake, TrackScheduler> schedulerMap = new HashMap<Snowflake, TrackScheduler>();

	/**
	 * A list of channel ids of channels that should be muted
	 */
	public static ArrayList<Snowflake> mutedChannels = new ArrayList<Snowflake>();

	public static CommandReceiver getInstance() {
		if (instance == null)
			instance = new CommandReceiver();
		return instance;
	}

	private CommandReceiver() {
		// scheduler = new TrackScheduler(PlayerManager.player);
	}

	/**
	 * Get the track scheduler for the guild of this event
	 * 
	 * @param event The message event
	 * @return The scheduler mapped to this channel
	 */
	public static TrackScheduler getScheduler(Snowflake channelId) {
		return schedulerMap.get(channelId);
	}

	/**
	 * Bot joins the same voice channel as the user who uses the command.
	 * 
	 * @param event The message event
	 * @return null
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
									Thread.sleep(250);
								} catch (InterruptedException e) {
									LOGGER.error(e.toString());
								}
							}

							// join returns a VoiceConnection which would be required if we were
							// adding disconnection features, but for now we are just ignoring it.
							TrackScheduler scheduler = new TrackScheduler();
							VoiceConnection vc = channel
									.join(spec -> spec.setProvider(new LavaPlayerAudioProvider(scheduler.getPlayer())))
									.block();

							vc.onConnectOrDisconnect().subscribe(s -> {
								Snowflake channelId = channel.getId();
								if (s.equals(State.CONNECTED)) {
									schedulerMap.put(channelId, scheduler);
								} else if (s.equals(State.DISCONNECTED)) {
									schedulerMap.remove(channelId);
								}
							});

						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Bot leaves any voice channel it is connected to in the same guild. Also
	 * clears the queue of items.
	 * 
	 * @param event The message event
	 * @return null
	 */
	public String leave(MessageCreateEvent event) {
		if (event != null) {
			if (event.getMessage() != null) {
				if (event.getMessage().getGuild() != null) {
					Guild guild = event.getMessage().getGuild().block();
					if (guild != null) {
						VoiceConnection botConnection = guild.getVoiceConnection().block();
						// If the client isn't in a voiceChannel, don't execute any other code
						if (botConnection == null) {
							// System.out.println("BOT NOT IN A VOICE CHANNEL");
							return null;
						}
						// get member who used command
						final Member member = event.getMember().orElse(null);
						if (member != null) {
							// get voice channel member is in
							final VoiceState voiceState = member.getVoiceState().block();
							if (voiceState != null) {
//							long botChannelId = botConnection.getChannelId().block().asLong();
								Snowflake memberChannelId = voiceState.getChannel().block().getId();
//							// check if user and bot are in the same channel
//							if (memberChannelId == botChannelId) {
//								botConnection.disconnect().block();
//								LOGGER.info("Bot disconnecting from voice channel.");
//								// System.out.println("DISCONNECTING");
//							}
								if (schedulerMap.containsKey(memberChannelId)) {
									schedulerMap.remove(memberChannelId);
								}
							}
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Bot replies with a simple echo message
	 * 
	 * @return "echo!"
	 */
	public String echo() {
		return ("echo!");
	}

	/**
	 * Bot rolls dice and returns results
	 * 
	 * @param params The number and type of dice to roll, eg "1d20"
	 * @return The results of the dice roll
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

	/**
	 * Attempts to play the link in the message
	 * 
	 * @param event  The message event
	 * @param params The link of the audio
	 * @return null
	 */
	public String play(TrackScheduler scheduler, String[] params) {
		if (scheduler != null) {
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
				PlayerManager.loadItem(params[0], scheduler);
				LOGGER.info("Loaded music item: " + params[0]);
			}
		}
		return null;
	}

	/**
	 * Sets the volume of the
	 * {@link com.sedmelluq.discord.lavaplayer.player.AudioPlayer}
	 * 
	 * @param event  The message event
	 * @param params The new volume setting
	 * @return Responds with new volume setting
	 */
	public String volume(TrackScheduler scheduler, String[] params) {
		if (scheduler != null) {
			if (params != null) {
				// final String content = event.getMessage().getContent();
				// final String[] command = content.split(" ");
				StringBuilder sb = new StringBuilder();
				if (params.length == 0) {
					return sb.append("Volume is currently " + scheduler.getPlayer().getVolume()).toString();
				} else if (params[0].equalsIgnoreCase("reset")) {
					scheduler.getPlayer().setVolume(TrackScheduler.DEFAULT_VOLUME);
					return sb.append("Volume reset to default").toString();
				}

				if (Pattern.matches("^[1-9][0-9]?$|^100$", params[0])) {
					int volume = Integer.parseInt(params[0]);
					sb.append("Changing volume from ").append(scheduler.getPlayer().getVolume()).append(" to ")
							.append(volume);
					scheduler.getPlayer().setVolume(volume);
					return sb.toString();
//					LOGGER.info(sb.toString());
//					MessageChannel channel = event.getMessage().getChannel().block();
//					if (channel != null)
//						channel.createMessage(sb.toString()).block();
				}

			}
		}
		return null;
	}

	/**
	 * Stops the LavaPlayer if it is playing anything
	 * 
	 * @param event The message event
	 * @return "Player stopped" if successful, null if not
	 */
	public String stop(TrackScheduler scheduler) {
		if (scheduler != null) {
			scheduler.getPlayer().stopTrack();
			LOGGER.info("Stopped music");
			return "Player stopped";
		}

		return null;
	}

	/**
	 * Stops the current song and plays the next in queue if there is any
	 * 
	 * @param event The message event
	 * @return The message event
	 */
	public String skip(TrackScheduler scheduler) {
		if (scheduler != null) {
			scheduler.nextTrack();
		}

		return null;
	}

	/**
	 * Mutes all {@link Member} in the channel besides bots
	 * 
	 * @param event The message event
	 * @return null
	 */
	public String mute(MessageCreateEvent event) {
		if (event != null && event.getMessage() != null && event.getMember().isPresent()) {
			// muteToggle = !muteToggle;

//				if (event.getMember().orElse(null).getVoiceState().block().getChannel().block().getId()
//						.asLong() != botChannelId) {
//					return;
//				}

			// has to use array to get around non-final variable inside of a lambda below
			boolean[] muted = { true };
			VoiceState userVoiceState = event.getMember().orElse(null).getVoiceState().block();
			// gets the member's channel who sent the message, and then all the VoiceStates
			// connected to that channel. From there we can get the Member of the VoiceState
			List<VoiceState> users = userVoiceState.getChannel().block().getVoiceStates().collectList().block();
			if (users != null) {

				// channel is muted, so unmute
				if (mutedChannels.contains(userVoiceState.getChannelId().get())) {
					muted[0] = false;
					mutedChannels.remove(userVoiceState.getChannelId().get());
				} else {
					// channel should be muted
					mutedChannels.add(userVoiceState.getChannelId().get());
				}
				for (VoiceState user : users) {

					// don't mute itself or other bots
					if (user.getMember().block().isBot())
						continue;

					// LOGGER.info("Muting user " + user.getUser().block().getUsername());
					// mute/unmute all users
					user.getMember().block().edit(spec -> spec.setMute(muted[0])).block();
				}

			}

		}

		return null;
	}

	/**
	 * Clears the current queue of all objects
	 * 
	 * @param event The message event
	 * @return "Queue cleared" if successful, null if not
	 */
	public String clearQueue(TrackScheduler scheduler) {
		if (scheduler != null) {
			scheduler.clearQueue();
			return "Queue cleared";
		}

		return null;
	}

	/**
	 * Returns a list of the currently queued songs
	 * 
	 * @param event The message event
	 * @return List of songs in the queue, or "The queue is empty" if empty
	 */
	public String viewQueue(TrackScheduler scheduler) {
		if (scheduler != null) {
			// get list of songs currently in the queue
			List<AudioTrack> queue = scheduler.getQueue();
			StringBuilder sb = new StringBuilder();
			// if the queue is not empty
			if (queue.size() > 0) {
				// print total number of songs
				sb.append("Number of songs in queue: ").append(queue.size()).append("\n");
				for (AudioTrack track : queue) {
					// print title and author of song on its own line
					sb.append("\"").append(track.getInfo().title).append("\"").append(" by ")
							.append(track.getInfo().author).append("\n");
				}
			} else {
				sb.append("The queue is empty");
			}

			String retString = sb.toString();

			// if the message is longer than 2000 character, trim it so that its not over
			// the max character limit.
			if (sb.toString().length() >= Message.MAX_CONTENT_LENGTH)
				retString = sb.substring(0, Message.MAX_CONTENT_LENGTH - 1);

			return retString;
		}
		return null;
	}

	/**
	 * Shuffles the songs currently in the queue
	 * 
	 * @param event The message event
	 * @return null
	 */
	public String shuffleQueue(TrackScheduler scheduler) {
		if (scheduler != null) {
			scheduler.shuffleQueue();
		}
		return null;
	}

	/**
	 * Return the info for the currently playing song
	 * 
	 * @param event The message event
	 * @return Info of song currently playing
	 */
	public String nowPlaying(TrackScheduler scheduler) {
		if (scheduler != null) {
			StringBuilder sb = new StringBuilder("Now playing: ");
			// get the track that's currently playing
			AudioTrack track = scheduler.getNowPlaying();
			if (track != null) {
				// add track title and author
				sb.append("\"").append(track.getInfo().title).append("\"").append(" by ")
						.append(track.getInfo().author);
			}

			return sb.toString();
		}
		return null;
	}

	/**
	 * Creates a poll in the channel
	 * 
	 * @param event The message event
	 * @return null
	 * 
	 *         TODO: Figure out a way to return embed so this method can return the
	 *         poll that should be sent instead of sending it itself. Maybe have
	 *         commands return a {@link Message} instead of a String.
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

	/**
	 * Pauses/unpauses the player
	 * 
	 * @param event The message event
	 * @return null
	 */
	public String pause(TrackScheduler scheduler) {
		if (scheduler != null)
			scheduler.pause(!scheduler.isPaused());

		return null;
	}

	/**
	 * Returns a list of all commands in the channel the message was sent.
	 * 
	 * @return List of available commands
	 */
	public String printCommands() {

		StringBuilder sb = new StringBuilder("Available commands:");
		Set<Entry<String, Command>> entries = Commands.getEntries();
		for (Entry<String, Command> entry : entries) {
			sb.append(", ").append(Commands.COMMAND_PREFIX).append(entry.getKey());
		}
		return sb.toString().replaceAll(":,", ":");

	}

	/**
	 * @param event  The message event
	 * @param params The position to move the current song to in seconds
	 * @return null
	 */
	public String seek(TrackScheduler scheduler, String[] params) {
		if (scheduler != null) {
			if (params != null) {
				if (params.length > 0) {
					try {
						int positionInSeconds = Integer.parseInt(params[0]);
						scheduler.seek(positionInSeconds);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	/**
	 * @param event  The message event
	 * @param params The amount of time in seconds to rewind
	 * @return null
	 */
	public String rewind(TrackScheduler scheduler, String[] params) {
		if (scheduler != null) {
			if (params != null) {
				if (params.length > 0) {
					try {
						int amountInSeconds = Integer.parseInt(params[0]);
						scheduler.rewind(amountInSeconds);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	/**
	 * @param event  The message event
	 * @param params The amount of time in seconds to fast forward
	 * @return null
	 */
	public String fastForward(TrackScheduler scheduler, String[] params) {
		if (scheduler != null) {
			if (params != null) {
				if (params.length > 0) {
					try {
						int amountInSeconds = Integer.parseInt(params[0]);
						scheduler.fastForward(amountInSeconds);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	public String cyberpunk() {

		return "Cyberpunk will relsae in; ";
	}

}
