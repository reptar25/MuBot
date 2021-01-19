package com.github.mubot.database;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.mubot.command.util.Pair;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class PrefixCollection {
	private static final String DEFAULT_COMMAND_PREFIX = "!";
	private static final Logger LOGGER = Loggers.getLogger(PrefixCollection.class);
	private static final ConcurrentHashMap<Long, String> PREFIX_MAP = new ConcurrentHashMap<Long, String>();

	private static final String getAllPrefixSQL = String.format("SELECT id, prefix FROM %s",
			DatabaseManager.getTableName());
	private static final String setPrefixSQL = String.format(
			"INSERT INTO %s (id, prefix) VALUES ($1, $2) ON CONFLICT (id) DO UPDATE SET prefix = $2",
			DatabaseManager.getTableName());

	private DatabaseManager databaseManager;
	private AtomicInteger counter;

	public PrefixCollection(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
		counter = new AtomicInteger(0);
		buildPrefixMap();
	}

	private void buildPrefixMap() {
		this.databaseManager.getClient()
				.inTransaction(handle -> handle.select(getAllPrefixSQL).mapResult(result -> result.map((row, rowMd) -> {
					return new Pair<Long, String>(row.get("id", Long.class), row.get("prefix", String.class));
				}))).doOnTerminate(() -> {
					LOGGER.info("PREFIX_MAP counter: " + counter.getAcquire());
				}).onErrorResume(error -> Mono.empty()).subscribe(pair -> {
					long id = pair.getKey();
					String prefix = pair.getValue();
					// LOGGER.info(String.format("put %s in %d", prefix, id));
					counter.getAndIncrement();
					PREFIX_MAP.put(id, prefix);
				});
	}

	public String getPrefix(long id) {
		String prefix = PREFIX_MAP.get(id);
		if (prefix == null || prefix.isBlank()) {
			return DEFAULT_COMMAND_PREFIX;
		}

		return prefix;
	}

	public Flux<Integer> setPrefix(long id, String prefix) {

		LOGGER.info(String.format("Adding prefix %s for server %d", prefix, id));

		return databaseManager.getClient().inTransaction(handle -> handle.execute(setPrefixSQL, id, prefix))
				.map(result -> {
					PREFIX_MAP.put(id, prefix);
					return result;
				});
	}

}