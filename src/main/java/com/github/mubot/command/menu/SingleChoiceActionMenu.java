package com.github.mubot.command.menu;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Member;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

/**
 * An ActionChoiceMenu that only listens for the first reaction of the message
 *
 */
public abstract class SingleChoiceActionMenu extends ChoiceActionMenu {
	private static final Logger LOGGER = Loggers.getLogger(ChoiceActionMenu.class);

	/**
	 * Adds a ReactionAddEvent listener to the menu to allow for some action to be
	 * performed when a non-bot member adds a reaction to this menu's message Takes
	 * only the first reaction or times-out after TIMEOUT duration
	 * 
	 * @return
	 */
	@Override
	protected Mono<Void> addReactionListener() {
		return message.getClient().on(ReactionAddEvent.class)
				.filter(e -> !e.getMember().map(Member::isBot).orElse(false))
				.filter(e -> e.getMessageId().asLong() == message.getId().asLong())
				.filter(e -> !e.getEmoji().asUnicodeEmoji().isEmpty()).take(TIMEOUT).take(1L).doOnTerminate(() -> {
					message.removeAllReactions().subscribe();
				}).flatMap(event -> {
					return loadSelection(event);
				}).onErrorResume(error -> {
					LOGGER.error("Error in reaction listener.", error);
					return Mono.empty();
				}).then();
	}

}
