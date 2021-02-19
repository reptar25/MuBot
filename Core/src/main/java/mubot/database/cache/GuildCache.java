package mubot.database.cache;

import discord4j.core.object.entity.Guild;
import mubot.command.util.Pair;
import mubot.database.DatabaseManager;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.concurrent.ConcurrentHashMap;

public class GuildCache extends DatabaseCache {
	private static final Logger LOGGER = Loggers.getLogger(DatabaseCache.class);
	private static final ConcurrentHashMap<Long, String> GUILD_CACHE = new ConcurrentHashMap<>();
	private final static String TABLE_NAME = "guilds";

	private static final String GET_ALL_GUILDS_SQL = "SELECT guild_id, guild_name FROM " + TABLE_NAME;
	private static final String INSERT_GUILD_SQL = "INSERT INTO " + TABLE_NAME
			+ " (guild_id, guild_name) VALUES ($1, $2) ON CONFLICT (guild_id) DO UPDATE SET guild_name = $2";
	private static final String DELETE_GUILD_SQL = "DELETE FROM " + TABLE_NAME + " WHERE guild_id = $1";
	private static final String UPDATE_GUILD_NAME_SQL = "UPDATE " + TABLE_NAME + " SET guild_name = $2 WHERE guild_id = $1";

	public GuildCache(DatabaseManager databaseManager) {
		super(databaseManager);
	}

	@Override
	public Mono<Void> buildCache() {
		return this.databaseManager.getClient().sql(GET_ALL_GUILDS_SQL).map((row,
				rowMd) -> new Pair<>(row.get("guild_id", Long.class), row.get("guild_name", String.class)))
				.all().doOnTerminate(() -> LOGGER.info("GUILD_CACHE count: " + counter.getAcquire())).onErrorResume(error -> Mono.empty()).map(pair -> {
					long id = pair.getKey();
					String name = pair.getValue();
					// LOGGER.info(String.format("put %s in %d", prefix, id));
					counter.getAndIncrement();
					GUILD_CACHE.put(id, name);
					return pair;
				}).then();

	}

	public Mono<Void> offerGuild(Guild guild) {
		String cachedName = GUILD_CACHE.get(guild.getId().asLong());
		if (cachedName == null) {
			return addGuild(guild.getId().asLong(), guild.getName());
		} else if (!cachedName.equals(guild.getName())) {
			return updateGuild(guild.getId().asLong(), guild.getName());
		}
		return Mono.empty();
	}

	private Mono<Void> addGuild(long guildId, String guildName) {
		return databaseManager.getClient().sql(INSERT_GUILD_SQL).bind("$1", guildId).bind("$2", guildName).fetch()
				.rowsUpdated().map(result -> {
					LOGGER.info(String.format("Adding guild %s with id %d", guildName, guildId));
					GUILD_CACHE.put(guildId, guildName);
					return result;
				}).then();
	}

	private Mono<Void> updateGuild(long guildId, String guildName) {
		return databaseManager.getClient().sql(UPDATE_GUILD_NAME_SQL).bind("$1", guildId).bind("$2", guildName).fetch()
				.rowsUpdated().map(result -> {
					LOGGER.info(String.format("Updating guild with id %d with new name %s", guildId, guildName));
					GUILD_CACHE.put(guildId, guildName);
					return result;
				}).then();
	}

	public Mono<? extends Void> removeGuild(long guildId) {
		return databaseManager.getClient().sql(DELETE_GUILD_SQL).bind("$1", guildId).fetch().rowsUpdated()
				.map(result -> {
					LOGGER.info(String.format("Removed guild id %d", guildId));
					PrefixCache.removePrefix(guildId);
					GUILD_CACHE.remove(guildId);
					return result;
				}).then();
	}

}
