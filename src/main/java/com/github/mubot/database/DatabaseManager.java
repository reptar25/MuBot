package com.github.mubot.database;

import io.r2dbc.client.R2dbc;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.client.SSLMode;

public class DatabaseManager {
	private final static String USERNAME = System.getenv("DATABASE_USERNAME");
	private final static String PWD = System.getenv("DATABASE_PWD");
	private final static String HOST = System.getenv("DATABASE_HOST");
	private final static String DATABASE_NAME = System.getenv("DATABASE_NAME");
	private final String TABLE_NAME = "guilds";

	private static PrefixCollection prefixCollection;
	private static DatabaseManager instance;
	private static R2dbc client;

	public static void create() {
		DatabaseManager.instance = new DatabaseManager();
		DatabaseManager.client = new R2dbc(new PostgresqlConnectionFactory(PostgresqlConnectionConfiguration.builder()
				.host(HOST).username(USERNAME).password(PWD).database(DATABASE_NAME).enableSsl().sslMode(SSLMode.REQUIRE).build()));

		DatabaseManager.prefixCollection = new PrefixCollection(DatabaseManager.instance);
	}

	public R2dbc getClient() {
		return client;
	}

	public static PrefixCollection getPrefixCollection() {
		return DatabaseManager.prefixCollection;
	}

	public static String getTableName() {
		return DatabaseManager.instance.TABLE_NAME;
	}

	public static DatabaseManager getInstance() {
		return DatabaseManager.instance;
	}
}