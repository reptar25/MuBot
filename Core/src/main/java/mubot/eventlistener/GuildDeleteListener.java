package mubot.eventlistener;

import discord4j.core.event.domain.guild.GuildDeleteEvent;
import mubot.api.API;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class GuildDeleteListener implements EventListener<GuildDeleteEvent> {

    private static final Logger LOGGER = Loggers.getLogger(GuildDeleteListener.class);

    @Override
    public Class<GuildDeleteEvent> getEventType() {
        return GuildDeleteEvent.class;
    }

    @Override
    public Mono<Void> consume(GuildDeleteEvent e) {
        return Mono.just(e).flatMap(event -> {
            LOGGER.info("GuildDeleteEvent consumed: " + event.getGuildId().asLong());
            String json = "{ \"guild_id\" : " + event.getGuildId().asLong() + " }";

            return Mono.just(API.getAPI().getGuildService().remove(json)).then();
            //return DatabaseManager.getInstance().getGuildCache().removeGuild(event.getGuildId().asLong());
        });
    }

}
