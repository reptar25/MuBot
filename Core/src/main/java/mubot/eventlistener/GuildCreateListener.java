package mubot.eventlistener;

import discord4j.core.event.domain.guild.GuildCreateEvent;
import mubot.api.API;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class GuildCreateListener implements EventListener<GuildCreateEvent> {

    private static final Logger LOGGER = Loggers.getLogger(GuildCreateListener.class);

    @Override
    public Class<GuildCreateEvent> getEventType() {
        return GuildCreateEvent.class;
    }

    @Override
    public Mono<Void> consume(GuildCreateEvent e) {
        return Mono.just(e).flatMap(event -> {
            LOGGER.info("GuildCreateEvent consumed: " + event.getGuild().getId().asLong() + ", "
                    + event.getGuild().getName());

            return Mono.just(API.getAPI().getGuildService().createOrUpdate(event.getGuild())).then();//DatabaseManager.getInstance().getGuildCache().offerGuild(event.getGuild());
        });
    }

}
