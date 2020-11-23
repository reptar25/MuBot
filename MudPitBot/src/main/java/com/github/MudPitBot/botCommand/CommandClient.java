package com.github.MudPitBot.botCommand;

import java.util.Arrays;
import java.util.Map.Entry;

import com.github.MudPitBot.botCommand.commandInterface.Command;
import com.github.MudPitBot.botCommand.commandInterface.Commands;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.util.Logger;
import reactor.util.Loggers;

/**
 * A client class for the Command design pattern. A client is an object that
 * controls the command execution process by specifying what commands to execute
 * and at what stages of the process to execute them.
 * https://www.baeldung.com/java-command-pattern
 */
public class CommandClient {

	private static final Logger LOGGER = Loggers.getLogger(CommandClient.class);
	private GatewayDiscordClient client;
	private CommandExecutor executor = new CommandExecutor();
	private static CommandClient instance;

	// Singleton create method
	public static CommandClient create(GatewayDiscordClient client) {
		if (instance == null)
			instance = new CommandClient(client);

		return instance;
	}

	private CommandClient(GatewayDiscordClient client) {
		this.client = client;

		setupListener();
		LOGGER.info(("Client created."));
	}

	/*
	 * Sets up a listener on the event dispatcher.
	 */
	private void setupListener() {
		/*
		 * Add listener for new messages being sentWhenever a messaged is typed in chat
		 * that the bot is able to see it should filter through this method.
		 */
		if (client.getEventDispatcher() != null) {
			client.getEventDispatcher().on(MessageCreateEvent.class)
					// subscribe is like block, in that it will *request* for action
					// to be done, but instead of blocking the thread, waiting for it
					// to finish, it will just execute the results asynchronously.
					.subscribe(event -> {
						if (event.getMessage() != null && event.getMessage().getContent() != null) {
							// 3.1 Message.getContent() is a String
							final String content = event.getMessage().getContent();

							// print out new message to logs
							logMessage(event, content);

							// process new message to check for commands
							processMessage(event, content);
						}
					});

			/*
			 * Add listener for members joining/changing voice channels.
			 */
			client.getEventDispatcher().on(VoiceStateUpdateEvent.class).subscribe(event -> {

				// Checks whether a member should be muted on joining a voice channel and mutes
				// them if so
				muteOnJoin(event);
			});
		}
	};

	/**
	 * Checks if the new voice channel is the channel the bot currently has muted,
	 * and mutes the member if it is
	 * 
	 * @param event event of the channel change
	 */
	private void muteOnJoin(VoiceStateUpdateEvent event) {

		if (event.isJoinEvent() || event.isMoveEvent() || event.isLeaveEvent()) {
			// if mute isnt toggled on just stop
			Snowflake newChannelId = event.getCurrent().getChannelId().orElse(null);
//			VoiceState oldChannel = event.getOld().orElse(null);
//			Snowflake outChannelId = null;
//			if (oldChannel != null)
//				outChannelId = event.getOld().orElse(null).getChannelId().orElse(null);

//			if (!CommandReceiver.mutedChannels.contains(newChannelId)
//					&& !CommandReceiver.mutedChannels.contains(outChannelId)) {
//				return;
//			}

			// if its a bot who joined, do not execute any more code
			if (event.getCurrent().getMember().block().isBot()) {
				return;
			}

			// if the newly joined channel is null just stop
			if (event.getCurrent().getChannelId() == null) {
				return;
			}

			// if user join same channel as mute channel
			if (CommandReceiver.mutedChannels.contains(newChannelId)) {
				// mute the member that just joined
				event.getCurrent().getMember().block().edit(spec -> spec.setMute(true)).block();
			}
			// if user joined another channel, make sure they arent muted still
			else {

				// if the channel their are leaving is the channel that was muted
				// unmute the leaving member
				Member member = event.getCurrent().getMember().block();
				VoiceState vs = member.getVoiceState().block();
				if (vs != null) {
					member.edit(spec -> spec.setMute(false)).block();
					LOGGER.info("Unmuting " + event.getCurrent().getUser().block().getUsername());
				}
			}
		}
	}

	/**
	 * Processes the message to check for commands and execute those commands
	 * 
	 * @param event   event of the message
	 * @param content content of the message
	 */
	public void processMessage(MessageCreateEvent event, String content) {

		// ignore any messages sent from a bot
		User sender = event.getMessage().getAuthor().orElse(null);
		if (sender != null) {
			if (sender.isBot()) {
				return;
			}
		}

		// split content at ! to allow for compound commands (more than 1 command in 1
		// message)
		// this regex splits at !, but doesn't remove it from the resulting string
		String[] commands = content.split("(?=" + Commands.COMMAND_PREFIX + ")");
		for (String command : commands) {
			command = command.trim();
			for (final Entry<String, Command> entry : Commands.getEntries()) {
				// We will be using ! as our "prefix" to any command in the system.
				String[] splitCommand = command.split(" ");
				if (splitCommand[0].toLowerCase().startsWith(Commands.COMMAND_PREFIX + entry.getKey().toLowerCase())) {
					String[] params = Arrays.copyOfRange(splitCommand, 1, splitCommand.length);

					// commands will return any string that the bot should send back as a message to
					// the command
					String returnMessage = executor.executeCommand(event, entry.getValue(), params);

					// if there is a message to send back send it to the channel the original
					// message was sent from
					if (returnMessage != null) {
						MessageChannel channel = event.getMessage().getChannel().block();
						if (channel != null) {
							channel.createMessage(returnMessage).block();
						}
					}

					break;
				}
			}
		}
	}

	/**
	 * Formats and logs the message content and author
	 * 
	 * @param event   even of the message
	 * @param content content of the message
	 */
	public void logMessage(MessageCreateEvent event, String content) {
		StringBuilder sb = new StringBuilder("New message created: ");

		Member member = event.getMember().orElse(null);

		// add the user name and put the message in quotes
		if (member != null) {
			sb.append(member.getUsername());
		} else {
			sb.append(event.getMessage().getAuthor().orElse(null).getUsername());
		}
		sb.append(" - \"").append(content).append("\"");
		LOGGER.info(sb.toString());
	}
}
