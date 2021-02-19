package mubot.command.menu;

import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

/**
 * An ActionChoiceMenu that only listens for the first reaction of the message
 */
public abstract class SingleChoiceActionMenu extends ChoiceActionMenu {
    private static final Logger LOGGER = Loggers.getLogger(ChoiceActionMenu.class);

    /**
     * Adds a ReactionAddEvent listener to the menu to allow for some action to be
     * performed when a non-bot member adds a reaction to this menu's message Takes
     * only the first reaction or times-out after TIMEOUT duration
     *
     * @return the mono to add a reaction listener
     */
    @Override
    protected Mono<Void> addReactionListener() {
        return getDefaultListener().take(1L).doOnTerminate(() -> message.removeAllReactions().onErrorResume(error -> Mono.empty()).subscribe()).flatMap(this::loadSelection).onErrorResume(error -> {
            LOGGER.error("Error in reaction listener.", error);
            return Mono.empty();
        }).then();
    }

}
