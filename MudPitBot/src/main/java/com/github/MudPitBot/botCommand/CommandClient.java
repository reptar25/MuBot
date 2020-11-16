package com.github.MudPitBot.botCommand;

import java.util.Map.Entry;

import com.github.MudPitBot.botCommand.commandImpl.Commands;
import com.github.MudPitBot.botCommand.commandInterface.Command;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.spec.Spec;
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

	// Singleton create method=
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
					final String content = event.getMessage().getContent().toLowerCase(); // 3.1 Message.getContent() is
																							// a String
					// System.out.println("MESSAGE CREATED: "+content);
					StringBuilder sb = new StringBuilder("New message created: ");
					// add the user name and put the message in quotes
					sb.append(event.getMember().orElse(null).getUsername()).append(" - \"").append(content)
							.append("\"");
					LOGGER.info(sb.toString());
					for (final Entry<String, Command> entry : Commands.COMMANDS.entrySet()) {
						// We will be using ! as our "prefix" to any command in the system.
						if (content.startsWith('!' + entry.getKey().toLowerCase())) {
							executor.executeCommand(entry.getValue(), event);
							break;
						}
					}
				});

		client.getEventDispatcher().on(VoiceStateUpdateEvent.class).subscribe(event -> {

			// if its a bot who joined, do not execute any more code
			if (event.getCurrent().getMember().block().isBot()) {
				return;
			}

			// get channel id of user who joined
			long userChannelId = event.getCurrent().getChannelId().orElse(null).asLong();
			// get bot channel id
			// long botChannelId =
			// client.getSelf().block().asMember(client.getSelfId()).block().getVoiceState().block()
			// .getChannelId().orElse(null).asLong();

			// if user join same channel as mute channel
			if (userChannelId == CommandReceiver.muteChannelId) {
				if (event.isJoinEvent() || event.isMoveEvent()) {
					// if the mute toggle is enabled
					if (CommandReceiver.muteToggle)
						// mute the member that just joined
						event.getCurrent().getMember().block().edit(spec -> spec.setMute(true)).block();
						LOGGER.info("Muting "+event.getCurrent().getUser().block().getUsername());
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
							LOGGER.info("Unmuting "+event.getCurrent().getUser().block().getUsername());
						}
					}
				}

			}
		});
	};

}
