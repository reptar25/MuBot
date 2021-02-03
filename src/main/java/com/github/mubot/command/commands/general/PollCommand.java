package com.github.mubot.command.commands.general;

import java.util.function.Consumer;

import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.help.CommandHelpSpec;
import com.github.mubot.command.menu.menus.PollMenu;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class PollCommand extends RequireBotPermissionsCommand {

	private final String REGEX_SPLIT = " (?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)";

	public PollCommand() {
		super("poll", Permission.MANAGE_MESSAGES);
	}

	@Override
	protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args) {
		return poll(pollArgs(args), event.getMember().orElse(null));
	}

	/**
	 * Converts regular command parameters to poll command parameters
	 * 
	 * @param args the original command parameters
	 * @return the command parameters split by double quotes instead of spaces
	 */

	private String[] pollArgs(String[] args) {
		String[] ret = args;
		StringBuilder sb = new StringBuilder();
		// unsplit the parameters
		for (String param : ret) {
			sb.append(param).append(" ");
		}
		// now split the command by what's inside the quotes instead of by space
		ret = sb.toString().split(REGEX_SPLIT);

		// now remove the double quotes from each parameter
		for (int i = 0; i < ret.length; i++) {
			ret[i] = ret[i].replaceAll("\"", "");
		}

		return ret;
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
	public Mono<CommandResponse> poll(@NonNull String[] args, @NonNull Member member) {
		if (args.length <= 0 || args[0].isBlank())
			return getHelp(member.getGuildId().asLong());
		// create a new poll object
		PollMenu poll = new PollMenu(args, member);

		return CommandResponse.create(poll.createMessage(), poll);
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription(
				"Creates a simple poll in the channel the command was used in. Allows up to 10 choices. All arguments must be contained in quotes to allow for spaces.")
				.addArg("Question", "The question for the poll in quotes(\").", false)
				.addArg("Choice 1", "The first choice of the poll in quotes(\").", false)
				.addArg("Choice 2", "The second choice of the poll in quotes(\").", false)
				.addArg("Choice X", "The X-th choice of the poll in quotes(\").", true)
				.addExample("\"question\" \"choice 1\" \"choice 2\"");
	}
}
