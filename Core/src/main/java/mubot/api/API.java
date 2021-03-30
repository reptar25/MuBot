package mubot.api;

import mubot.api.services.GuildService;
import mubot.api.services.PrefixService;

public class API {

    private static API instance = null;
    private final GuildService guildService;
    private final PrefixService prefixService;

    private API() {
        AccessToken token = AccessToken.createAccessToken();
        this.guildService = new GuildService(token);
        this.prefixService = new PrefixService(token);
    }

    public static API getAPI() {
        if (instance == null)
            instance = new API();

        return instance;
    }

    public GuildService getGuildService() {
        return guildService;
    }

    public PrefixService getPrefixService() {
        return prefixService;
    }
}
