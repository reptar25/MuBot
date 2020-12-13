package com.github.MudPitBot.command.impl;

import java.util.function.Consumer;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.misc.Poll;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

public class PollCommand extends Command {

	public PollCommand() {
		super("poll");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] params) {
		return poll(pollParams(params), event.getMember().orElse(null));
	}

	/**
	 * Converts regular command parameters to poll command parameters
	 * 
	 * @param params the original command parameters
	 * @return the command parameters split by double quotes instead of spaces
	 */
	private String[] pollParams(String[] params) {
		StringBuilder sb = new StringBuilder();
		// unsplit the parameters
		for (String param : params) {
			sb.append(param).append(" ");
		}
		// now split the command by what's inside the quotes instead of by space
		params = sb.toString().split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

		// now remove the double quotes from each parameter
		for (int i = 0; i < params.length; i++) {
			params[i] = params[i].replaceAll("\"", "");
		}

		return params;
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
	public Mono<CommandResponse> poll(String[] params, Member member) {
		// create a new poll object
		Poll poll = new Poll(params, member);

		// if the poll is invalid just stop
		if (poll.getAnswers().size() <= 1) {
			return Mono.empty();
		}

		// create the embed to put the poll into
		Consumer<? super MessageCreateSpec> spec = s1 -> s1
				.setEmbed(s2 -> s2.setColor(Color.of(23, 53, 77)).setFooter(poll.getFooter(), poll.getFooterURL())
						.setTitle(poll.getTitle()).setDescription(poll.getDescription()));

		return Mono.just(new CommandResponse.Builder().spec(spec).poll(poll).build());

	}

}
