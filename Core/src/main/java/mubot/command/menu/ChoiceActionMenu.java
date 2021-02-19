package mubot.command.menu;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;

/**
 * A menu that does some action after a user chooses a reaction
 *
 */
public abstract class ChoiceActionMenu extends Menu {

	private static final Logger LOGGER = Loggers.getLogger(ChoiceActionMenu.class);
	protected final Duration TIMEOUT = Duration.ofMinutes(5L);

	/**
	 * Adds a ReactionAddEvent listener to the menu to allow for some action to be
	 * performed when a non-bot member adds a reaction to this menu's message.
	 * Times-out after TIMEOUT duration
	 * 
	 * @return the mono to add a reaction listener
	 */
	protected Mono<Void> addReactionListener() {

		return getDefaultListener().doOnTerminate(() -> message.removeAllReactions().onErrorResume(error -> Mono.empty()).subscribe()).flatMap(this::loadSelection).onErrorResume(error -> {
			LOGGER.error("Error adding reaction listener.", error);
			return Mono.empty();
		}).then();
	}

	protected Flux<ReactionAddEvent> getDefaultListener() {
		return message.getClient().on(ReactionAddEvent.class)
				.filter(e -> !e.getMember().map(Member::isBot).orElse(false))
				.filter(e -> e.getMessageId().asLong() == message.getId().asLong())
				.filter(e -> e.getEmoji().asUnicodeEmoji().isPresent()).take(TIMEOUT);
	}

	@Override
	public void setMessage(Message message) {
		super.setMessage(message);
		// message is set so add a reaction listener and reactions to the message
		addReactions().then(addReactionListener()).onErrorResume(error -> {
			LOGGER.error("Error adding reactions.", error);
			return Mono.empty();
		}).subscribe();
	}

	/**
	 * Adds reactions to the message as choices for the user to select
	 * 
	 * @return the mono to add reactions
	 */
	protected abstract Mono<Void> addReactions();

	/**
	 * Called when a reaction is added to this message, this is where the "action"
	 * of the menu should happen
	 * 
	 * @param event the ReactionAddEvent event
	 * @return the mono to load the selected item
	 */
	protected abstract Mono<Void> loadSelection(ReactionAddEvent event);
}
