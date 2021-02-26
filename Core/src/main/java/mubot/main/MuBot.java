package mubot.main;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import mubot.database.DatabaseManager;
import mubot.eventlistener.*;
import mubot.heroku.HerokuServer;
import reactor.util.Logger;
import reactor.util.Loggers;

public class MuBot {

    private static final Logger LOGGER = Loggers.getLogger(MuBot.class);

    private final GatewayDiscordClient client;

    public MuBot(GatewayDiscordClient client) {
        this.client = client;
        // we should only find this when running on Heroku
        String port = System.getenv("PORT");
        if (port != null) {
            // bind PORT for Heroku integration
            try {
                HerokuServer.create(Integer.parseInt(port));
            } catch (NumberFormatException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } else {
            LOGGER.info("Not running on Heroku");
            // only log messages on the local client
            registerListener(new MessageLogger());
        }

        DatabaseManager.create();
        registerListener(new ReadyListener());
        registerListener(new VoiceStateUpdateListener());
        registerListener(new CommandListener());
        registerListener(new MuteOnJoinListener());
        registerListener(new GuildCreateListener());
        registerListener(new GuildDeleteListener());

    }

    private <T extends Event> void registerListener(EventListener<T> listener) {
        client.getEventDispatcher();
        client.getEventDispatcher().on(listener.getEventType())
                .flatMap(listener::consume)
                .subscribe(null,
                        error -> LOGGER.error("Error in listener with type " + listener.getEventType() + ": " + error.getMessage()));
    }

}
