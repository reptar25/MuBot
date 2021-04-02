package mubot.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.mashape.unirest.request.body.RequestBodyEntity;
import mubot.api.AccessToken;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.concurrent.ConcurrentHashMap;

public class PrefixService extends CrudService {

    private static final ConcurrentHashMap<Long, String> PREFIX_CACHE = new ConcurrentHashMap<>();
    private static final String DEFAULT_COMMAND_PREFIX = "!";
    private static final Logger LOGGER = Loggers.getLogger(PrefixService.class);

    public PrefixService(AccessToken token) {
        super(token, "/prefixes");
        buildCache();
    }

    private void buildCache() {
        com.mashape.unirest.http.JsonNode prefixes = getAll();
        prefixes.getArray().forEach(guild -> {
            try {
                com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(guild.toString());
                PREFIX_CACHE.put(node.get("guild_id").asLong(), node.get("prefix").asText());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });

        LOGGER.info("prefix cache size = " + PREFIX_CACHE.size());
    }

    @Override
    public RequestBodyEntity createOrUpdate(Object body) {

        try {
            JsonNode json = mapper.readTree(body.toString());
            PREFIX_CACHE.put(json.get("guild_id").asLong(), json.get("prefix").asText());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return super.createOrUpdate(body);
    }

    @Override
    public String getById(long id) {
        if (PREFIX_CACHE.containsKey(id))
            return PREFIX_CACHE.get(id);

        String prefix = super.getById(id, "prefix");

        if (prefix == null || prefix.isEmpty())
            prefix = DEFAULT_COMMAND_PREFIX;

        PREFIX_CACHE.put(id, prefix);
        return prefix;
    }
}
