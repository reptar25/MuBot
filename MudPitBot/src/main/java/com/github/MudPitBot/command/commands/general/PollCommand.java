package com.github.MudPitBot.command.commands.general;

import static com.github.MudPitBot.command.CommandUtil.requireBotPermissions;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.menu.PollMenu;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

public class PollCommand extends Command {

	public PollCommand() {
		super("poll");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return event.getMessage().getChannel()
				.flatMap(channel -> requireBotPermissions((GuildChannel) channel, Permission.MANAGE_MESSAGES))
				.flatMap(ignored -> poll(pollArgs(args), event.getMember().orElse(null)));
	}

	/**
	 * Converts regular command parameters to poll command parameters
	 * 
	 * @param args the original command parameters
	 * @return the command parameters split by double quotes instead of spaces
	 */
	private final String REGEX_SPLIT = " (?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)";

	private String[] pollArgs(String[] args) {
		StringBuilder sb = new StringBuilder();
		// unsplit the parameters
		for (String param : args) {
			sb.append(param).append(" ");
		}
		// now split the command by what's inside the quotes instead of by space
		args = sb.toString().split(REGEX_SPLIT);

		// now remove the double quotes from each parameter
		for (int i = 0; i < args.length; i++) {
			args[i] = args[i].replaceAll("\"", "");
		}

		return args;
	}

	/**
	 * Creates a poll in the channel
	 * 
	 * @param member
	 * 
	 * @param event  The message event
	 * @return null
	 * 
	 */
	public Mono<CommandResponse> poll(String[] args, Member member) {
		// create a new poll object
		PollMenu poll = new PollMenu(args, member);

		return new CommandResponse.Builder().withCreateSpec(poll.createMessage()).withMenu(poll).build();
	}

}
