package mubot.database.cache;

import mubot.command.util.Pair;
import mubot.database.DatabaseManager;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.concurrent.ConcurrentHashMap;

public class PrefixCache extends DatabaseCache {
	private static final String DEFAULT_COMMAND_PREFIX = "!";
	private static final Logger LOGGER = Loggers.getLogger(PrefixCache.class);

	private static final ConcurrentHashMap<Long, String> PREFIX_CACHE = new ConcurrentHashMap<>();
	private final static String TABLE_NAME = "prefixes";

	private static final String GET_ALL_PREFIX_SQL = "SELECT guild_id, prefix FROM " + TABLE_NAME;
	private static final String INSERT_PREFIX_SQL = "INSERT INTO " + TABLE_NAME
			+ " (guild_id, prefix) VALUES ($1, $2) ON CONFLICT (guild_id) DO UPDATE SET prefix = $2";

	public PrefixCache(DatabaseManager databaseManager) {
		super(databaseManager);
	}

	@Override
	public Mono<Void> buildCache() {
		return this.databaseManager.getClient().sql(GET_ALL_PREFIX_SQL).map((row,
				rowMd) -> new Pair<>(row.get("guild_id", Long.class), row.get("prefix", String.class)))
				.all().doOnTerminate(() -> LOGGER.info("PREFIX_CACHE count: " + counter.getAcquire())).onErrorResume(error -> Mono.empty()).map(pair -> {
					long id = pair.getKey();
					String prefix = pair.getValue();
					// LOGGER.info(String.format("put %s in %d", prefix, id));
					counter.getAndIncrement();
					PREFIX_CACHE.put(id, prefix);
					return pair;
				}).then();

	}

	public String getPrefix(long id) {
		String prefix = PREFIX_CACHE.get(id);
		if (prefix == null || prefix.isBlank()) {
			return DEFAULT_COMMAND_PREFIX;
		}

		return prefix;
	}

	public Mono<Void> addPrefix(long id, String prefix) {
		return databaseManager.getClient().sql(INSERT_PREFIX_SQL).bind("$1", id).bind("$2", prefix).fetch()
				.rowsUpdated().map(result -> {
					LOGGER.info(String.format("Adding prefix %s for server %d", prefix, id));
					PREFIX_CACHE.put(id, prefix);
					return result;
				}).then();
	}

	public static void removePrefix(long guildId) {
		PREFIX_CACHE.remove(guildId);
	}
}
