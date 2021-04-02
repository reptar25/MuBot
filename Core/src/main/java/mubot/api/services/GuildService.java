package mubot.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.request.body.RequestBodyEntity;
import discord4j.core.object.entity.Guild;
import mubot.api.AccessToken;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.concurrent.ConcurrentHashMap;

public class GuildService extends CrudService {

    private static final ConcurrentHashMap<Long, String> GUILD_CACHE = new ConcurrentHashMap<>();
    private static final Logger LOGGER = Loggers.getLogger(GuildService.class);

    public GuildService(AccessToken token) {
        super(token, "/guilds");
        buildCache();
    }

    private void buildCache() {
        JsonNode guilds = getAll();
        guilds.getArray().forEach(guild -> {
            try {
                com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(guild.toString());
                GUILD_CACHE.put(node.get("guild_id").asLong(), node.get("guild_name").asText());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });

        LOGGER.info("guild cache size = " + GUILD_CACHE.size());
    }

    @Override
    public RequestBodyEntity createOrUpdate(Object body) {
        if (!(body instanceof Guild))
            throw new IllegalArgumentException("Not a guild");

        Guild g = (Guild) body;

        String json = "{ \"guild_id\" : \"" + g.getId().asLong() + "\", \"guild_name\" : \"" + g.getName() + "\"}";
        GUILD_CACHE.put(g.getId().asLong(), g.getName());
        return super.createOrUpdate(json);
    }

    public String getById(long id) {
        if (GUILD_CACHE.containsKey(id))
            return GUILD_CACHE.get(id);

        try {
            String json = mapper.readTree(super.getById(id)).asText();
            GUILD_CACHE.put(id, json);
            return json;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return null;
        }

    }
}
