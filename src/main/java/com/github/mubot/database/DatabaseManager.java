package com.github.mubot.database;

import java.net.URI;
import java.net.URISyntaxException;

import io.r2dbc.client.R2dbc;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.client.SSLMode;

public class DatabaseManager {
	// for heroku DATABASE_URL is stored as postgres://<username>:<password>@<host>/<dbname>
	private final static String DB_URL = System.getenv("DATABASE_URL");
	private final String USERNAME;
	private final String PASSWORD;
	private final String HOST;
	private final int PORT;
	private final String DATABASE_NAME;
	private final String TABLE_NAME = "guilds";

	private static PrefixCollection prefixCollection;
	private static DatabaseManager instance;
	private static R2dbc client;

	private DatabaseManager() {
		URI dbUri = null;
		try {
			dbUri = new URI(DB_URL);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		this.USERNAME = dbUri.getUserInfo().split(":")[0];
		this.PASSWORD = dbUri.getUserInfo().split(":")[1];
		this.HOST = dbUri.getHost();
		this.PORT = dbUri.getPort();
		this.DATABASE_NAME = dbUri.getPath().replaceFirst("/", "");
	}

	public static void create() {
		DatabaseManager.instance = new DatabaseManager();
		DatabaseManager.client = new R2dbc(new PostgresqlConnectionFactory(PostgresqlConnectionConfiguration.builder()
				.host(DatabaseManager.instance.getHost()).port(DatabaseManager.instance.getPort())
				.username(DatabaseManager.instance.getUsername()).password(DatabaseManager.instance.getPassword())
				.database(DatabaseManager.instance.getDatabaseName()).enableSsl().sslMode(SSLMode.REQUIRE).build()));

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

	public String getUsername() {
		return USERNAME;
	}

	public String getPassword() {
		return PASSWORD;
	}

	public String getHost() {
		return HOST;
	}

	public String getDatabaseName() {
		return DATABASE_NAME;
	}

	public int getPort() {
		return PORT;
	}
}