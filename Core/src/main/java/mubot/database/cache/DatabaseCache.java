package mubot.database.cache;

import mubot.database.DatabaseManager;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class DatabaseCache {

	protected DatabaseManager databaseManager;
	protected AtomicInteger counter;

	public DatabaseCache(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
		counter = new AtomicInteger(0);
		buildCache().block();
	}

	public abstract Mono<Void> buildCache();
}
