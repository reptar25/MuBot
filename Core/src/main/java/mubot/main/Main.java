package mubot.main;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import reactor.util.Logger;
import reactor.util.Loggers;

public class Main {

    private static final Logger LOGGER = Loggers.getLogger(Main.class);

    public static void main(String[] args) {
        String discordApiToken = System.getenv("token");
        try {
            if (discordApiToken == null)
                discordApiToken = args[0];
        } catch (ArrayIndexOutOfBoundsException ignored) {
            LOGGER.error(
                    "No Discord api token found. Your Discord api token needs to be first argument or an env var named \"token\"");
            return;
        }

        // no token found
        if (discordApiToken == null) {
            LOGGER.error(
                    "No Discord api token found. Your Discord api token needs to be first argument or an env var named \"token\"");
            return;
        }

        final GatewayDiscordClient client = DiscordClientBuilder.create(discordApiToken).build().login().block();
        new MuBot(client);

        LOGGER.info("Bot is ready");

        if (client != null)
            client.onDisconnect().block();
    }

}
