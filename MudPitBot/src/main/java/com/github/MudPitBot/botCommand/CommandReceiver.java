package com.github.MudPitBot.botCommand;

import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

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
	}

	/*
	 * Bot leaves any voice channel it has previously joined into using the join
	 * command
	 */
	public void leave(MessageCreateEvent event) {
		if (currentVoiceConnection != null) {
			currentVoiceConnection.disconnect().block();
			LOGGER.info("Discconecting from channel");
		}
	}

	/*
	 * Bot replies with a simple echo message
	 */
	public void echo(MessageCreateEvent event) {
		if (event != null) {
			if (event.getMessage() != null && event.getClient() != null) {
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
	public void play(MessageCreateEvent event, String[] params) {
		if (event != null) {
			if (event.getMessage() != null) {
				if (event.getMessage().getContent() != null) {
					
					// unpause
					if(params[0].isEmpty() && scheduler.isPaused()) {
						scheduler.pause(false);
						return;
					}
					
					if (params.length <= 0 || params.length > 1 || params[0].isEmpty()) {
						LOGGER.error("Too many or few params for play");
						return;
					}
					PlayerManager.playerManager.loadItem(params[0], scheduler);
					LOGGER.info("Loaded music item: "+params[0]);
				}
			}
		}
	}

	/*
	 * Sets the volume of the LavaPlayer
	 */
	public void volume(MessageCreateEvent event, String[] params) {
		if (event != null && event.getMessage() != null) {
			if(params == null)
				return;
			//final String content = event.getMessage().getContent();
			//final String[] command = content.split(" ");
			if (params.length <= 0 || params.length > 1) {
				LOGGER.error("Too many or few params for volume");
			}

			if (Pattern.matches("[1-9]*[0-9]*[0-9]", params[0])) {
				int volume = Integer.parseInt(params[0]);
				PlayerManager.player.setVolume(volume);
				StringBuilder sb = new StringBuilder("Set volume to ").append(volume);
				LOGGER.info(sb.toString());
				MessageChannel channel = event.getMessage().getChannel().block();
				if (channel != null)
					channel.createMessage(sb.toString()).block();
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

	/*
	 * Mutes all {@link Member} in the channel besides bots and itself
	 */

	public void mute(MessageCreateEvent event) {
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
	}

	/*
	 * Clears the current queue of all objects
	 */
	public void clearQueue(MessageCreateEvent event) {
		if (event != null) {
			scheduler.clearQueue();
		}
	}

	/*
	 * Prints out a list of the currently queued songs
	 */
	public void viewQueue(MessageCreateEvent event) {
		if (event != null) {
			if (event.getMessage() != null && event.getClient() != null) {
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
					sb.append("The queue is empty.");
				}

				MessageChannel channel = event.getMessage().getChannel().block();
				if (channel != null) {
					// send back message to channel we had received the command in
					String messageString = sb.toString();

					// if the message is longer than 2000 character, trim it so that its not over
					// the max character limit.
					if (sb.toString().length() >= Message.MAX_CONTENT_LENGTH)
						messageString = sb.substring(0, Message.MAX_CONTENT_LENGTH - 1);

					channel.createMessage(messageString).block();
				}
			}
		}
	}

	/*
	 * Print out the info for the currently playing song
	 */
	public void nowPlaying(MessageCreateEvent event) {
		if (event != null) {
			if (event.getMessage() != null) {
				StringBuilder sb = new StringBuilder("Now playing: ");
				// get the track that's currently playing
				AudioTrack track = scheduler.getNowPlaying();
				if (track != null) {
					// add track title and author
					sb.append("\"").append(track.getInfo().title).append("\"").append(" by ")
							.append(track.getInfo().author);
				}

				MessageChannel channel = event.getMessage().getChannel().block();
				if (channel != null && event.getClient() != null) {
					// send back message to channel we had received the command in
					channel.createMessage(sb.toString()).block().addReaction(null);
				}
			}
		}
	}

	/*
	 * Creates a poll in the channel
	 */
	public void poll(MessageCreateEvent event, String[] params) {
		if (event != null) {
			if (event.getClient() != null) {
				if (event.getMessage() != null) {
					MessageChannel channel = event.getMessage().getChannel().block();
					if (channel != null) {
						// create a new poll object
						Poll poll = new Poll.Builder(event, params).build();

						// if the poll is invalid just stop
						if (poll.getAnswers().size() <= 1) {
							return;
						}

						// create the embed to put the poll into
						Message message = channel.createEmbed(spec -> spec.setColor(Color.of(23, 53, 77))
								.setFooter(poll.getFooter(), poll.getFooterURL()).setTitle(poll.getTitle())
								.setDescription(poll.getDescription())).block();

						if (message != null) {
							// add reactions as vote tickers, number of reactions depends on number of answers
							poll.addReactions(message);
							// message.pin().block();
						}
					}
				}
			}
		}
	}

	public void pause() {
		scheduler.pause(!scheduler.isPaused());
	}
}
