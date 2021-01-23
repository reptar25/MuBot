package com.github.mubot.database.cache;

import java.util.concurrent.ConcurrentHashMap;
import com.github.mubot.command.util.Pair;
import com.github.mubot.database.DatabaseManager;

import discord4j.core.object.entity.Guild;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class GuildCache extends DatabaseCache {
	private static final Logger LOGGER = Loggers.getLogger(DatabaseCache.class);
	private static final ConcurrentHashMap<Long, String> GUILD_CACHE = new ConcurrentHashMap<>();
	private final static String TABLE_NAME = "guilds";

	private static final String GET_ALL_GUILDS_SQL = "SELECT guild_id, guild_name FROM " + TABLE_NAME;
	private static final String INSERT_GUILD_SQL = "INSERT INTO " + TABLE_NAME
			+ " (guild_id, guild_name) VALUES ($1, $2) ON CONFLICT (guild_id) DO UPDATE SET guild_name = $2";
	private static final String DELETE_GUILD_SQL = "DELETE FROM " + TABLE_NAME + " WHERE guild_id = $1";

	public GuildCache(DatabaseManager databaseManager) {
		super(databaseManager);
	}

	@Override
	public Mono<Void> buildCache() {
		return this.databaseManager.getClient().sql(GET_ALL_GUILDS_SQL).map((row,
				rowMd) -> new Pair<Long, String>(row.get("guild_id", Long.class), row.get("guild_name", String.class)))
				.all().doOnTerminate(() -> {
					LOGGER.info("GUILD_CACHE count: " + counter.getAcquire());
				}).onErrorResume(error -> Mono.empty()).map(pair -> {
					long id = pair.getKey();
					String name = pair.getValue();
					// LOGGER.info(String.format("put %s in %d", prefix, id));
					counter.getAndIncrement();
					GUILD_CACHE.put(id, name);
					return pair;
				}).then();

	}

	public Mono<Void> offerGuild(Guild guild) {
		if (GUILD_CACHE.get(guild.getId().asLong()) == null) {
			long guildId = guild.getId().asLong();
			String guildName = guild.getName();
			return addGuild(guildId, guildName);
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
