package com.github.MudPitBot.botCommand;

import java.util.Map.Entry;

import com.github.MudPitBot.botCommand.commandImpl.Commands;
import com.github.MudPitBot.botCommand.commandInterface.Command;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
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
	
	public static CommandClient create(GatewayDiscordClient client) {
		
		if(instance == null)
			instance = new CommandClient(client);
		
		return instance;
	}
	
	private CommandClient(GatewayDiscordClient client) 
	{
		this.client = client;
		
		setupListener();
		LOGGER.info(("Client created."));
	}

	/*
	 * Sets up a listener on the event dispatcher for when messages are created. Whenever a messaged is typed in chat that the bot is able to see it should filter 
	 * through this method.
	 */
	private void setupListener() {
		client.getEventDispatcher().on(MessageCreateEvent.class)
	    // subscribe is like block, in that it will *request* for action
	    // to be done, but instead of blocking the thread, waiting for it
	    // to finish, it will just execute the results asynchronously.
	    .subscribe(event -> {
	        final String content = event.getMessage().getContent().toLowerCase(); // 3.1 Message.getContent() is a String
	        //System.out.println("MESSAGE CREATED: "+content);
	        StringBuilder sb = new StringBuilder("New message created :");
	        sb.append(event.getMember().orElse(null).getUsername()).append(" - \"").append(content).append("\"");
	        LOGGER.info(sb.toString());
	        for (final Entry<String, Command> entry : Commands.COMMANDS.entrySet()) {
	            // We will be using ! as our "prefix" to any command in the system.
	            if (content.startsWith('!' + entry.getKey().toLowerCase())) {
	            	executor.executeCommand(entry.getValue(), event);
	                break;
	            }
	        }
	    });
	};
	

}
