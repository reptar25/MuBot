package com.github.MudPitBot.command.commands.general;

import com.github.MudPitBot.JokeAPI.JokeClient;
import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.menu.menus.JokeMenu;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import static com.github.MudPitBot.command.util.Permissions.requireNotPrivate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JokeCommand extends Command {

	public JokeCommand() {
		super("joke");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireNotPrivate(event).flatMap(ignored -> joke(args));
	}

	private Mono<CommandResponse> joke(@NonNull String[] args) {
		boolean unsafe = false;
		JokeMenu menu = null;
		if (args.length > 0) {
			List<String> argList = Arrays.asList(args);
			unsafe = argList.contains("unsafe");
			List<String> categories = JokeClient.getJokeService().getCategories().map(List::stream)
					.map(s -> s.map(String::toLowerCase).collect(Collectors.toList())).block();
			for (String arg : argList) {
				if (categories.contains(arg.toLowerCase())) {
					// no safe-dark jokes, so ignore
					if (arg.equals("dark") && !unsafe)
						break;

					menu = new JokeMenu(unsafe, arg);
					break;
				}
			}
		}

		if (menu == null)
			menu = new JokeMenu(unsafe);

		return CommandResponse.create(menu.createMessage(), menu);
	}

	@Override
	public Mono<CommandResponse> getHelp() {
		return createCommandHelpEmbed(s -> s.setDescription("Tells a random joke from the chosen category of jokes.")
				.addArg("unsafe", "Allows \"unsafe\" jokes to be returned by the bot.", true)
				.addArg("category", "Gets a joke of only the given category.", true).addExample("puns")
				.addExample("unsafe").addExample("unsafe any").addExample("misc unsafe"));
	}

}
