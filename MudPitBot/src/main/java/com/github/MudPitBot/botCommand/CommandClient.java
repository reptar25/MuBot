package com.github.MudPitBot.botCommand;

import java.util.Map.Entry;

import com.github.MudPitBot.botCommand.commandImpl.Commands;
import com.github.MudPitBot.botCommand.commandInterface.Command;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import reactor.util.Logger;
import reactor.util.Loggers;

/*
 *  A client class for the Command design pattern. A client is an object that controls the command execution process
 *  by specifying what commands to execute and at what stages of the process to execute them.
 *  https://www.baeldung.com/java-command-pattern
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
		client.getEventDispatcher().on(MessageCreateEvent.class)
				// subscribe is like block, in that it will *request* for action
				// to be done, but instead of blocking the thread, waiting for it
				// to finish, it will just execute the results asynchronously.
				.subscribe(event -> {
					if (event.getMessage() != null && event.getMessage().getContent() != null) {
						// 3.1 Message.getContent() is a String
						final String content = event.getMessage().getContent().toLowerCase();

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

			// Checks whether a member should be muted on joining a voice channel and mutes them if so
			muteOnJoin(event);
		});
	};

	/**
	 * Checks if the new voice channel is the channel the bot currently has muted, and mutes the member if it is
	 * 
	 * @param event event of the channel change
	 */
	private void muteOnJoin(VoiceStateUpdateEvent event) {

		if (!CommandReceiver.muteToggle) {
			return;
		}

		// if its a bot who joined, do not execute any more code
		if (event.getCurrent().getMember().block().isBot()) {
			return;
		}

		if (event.getCurrent().getChannelId() == null) {
			return;
		}
		// get channel id of user who joined
		long userChannelId = event.getCurrent().getChannelId().orElse(null).asLong();

		// if user join same channel as mute channel
		if (userChannelId == CommandReceiver.muteChannelId) {
			if (event.isJoinEvent() || event.isMoveEvent()) {
				// if the mute toggle is enabled
				if (CommandReceiver.muteToggle)
					// mute the member that just joined
					event.getCurrent().getMember().block().edit(spec -> spec.setMute(true)).block();
				LOGGER.info("Muting " + event.getCurrent().getUser().block().getUsername());
			}
		}
		// if user joined another channel, make sure they arent muted still
		else {
			VoiceState oldVoiceState = event.getOld().orElse(null);
			if (oldVoiceState != null) {

				if (event.isMoveEvent() || event.isMoveEvent()) {
					long oldChannelId = oldVoiceState.getChannelId().orElse(null).asLong();
					// if the channel their are leaving is the channel that was muted
					if (oldChannelId == CommandReceiver.muteChannelId) {
						// if the mute toggle is enabled
						if (CommandReceiver.muteToggle)
							// unmute the leaving member
							event.getCurrent().getMember().block().edit(spec -> spec.setMute(false)).block();
						LOGGER.info("Unmuting " + event.getCurrent().getUser().block().getUsername());
					}
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
	private void processMessage(MessageCreateEvent event, String content) {
		// split content at ! to allow for compound commands (more than 1 command in 1
		// message)
		String[] commands = content.split("(?=!)");
		for (String command : commands) {
			command = command.trim();
			for (final Entry<String, Command> entry : Commands.entries()) {
				// We will be using ! as our "prefix" to any command in the system.
				if (command.startsWith('!' + entry.getKey().toLowerCase())) {
					command = command.replaceAll('!'+ entry.getKey().toLowerCase(), "").trim();
					String[] params = command.split(" ");
					executor.executeCommand(event, entry.getValue(), params);
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
	private void logMessage(MessageCreateEvent event, String content) {
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
