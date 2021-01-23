package com.github.mubot.database.cache;

import java.util.concurrent.atomic.AtomicInteger;

import com.github.mubot.database.DatabaseManager;

import reactor.core.publisher.Mono;

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
