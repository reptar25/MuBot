package com.github.MudPitBot.command.commands.general;

import static com.github.MudPitBot.command.util.Permissions.requireBotPermissions;
import static com.github.MudPitBot.command.util.Permissions.requireNotPrivate;
import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.menu.menus.PollMenu;

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
		return requireNotPrivate(event).flatMap(ignored -> event.getMessage().getChannel())
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

		return CommandResponse.create(poll.createMessage(), poll);
	}

	@Override
	public Mono<CommandResponse> getHelp() {
		return createCommandHelpEmbed(s -> s.setDescription(
				"Creates a simple poll in the channel the command was used in. Allows up to 10 choices. All arguments must be contained in quotes to allow for spaces.")
				.addArg("Question", "The question for the poll in quotes(\").", false)
				.addArg("Choice 1", "The first choice of the poll in quotes(\").", false)
				.addArg("Choice 2", "The second choice of the poll in quotes(\").", false)
				.addArg("Choice X", "The X-th choice of the poll in quotes(\").", true)
				.addExample("\"question\" \"choice 1\" \"choice 2\""));
	}

}
