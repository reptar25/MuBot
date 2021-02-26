package mubot.database;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.client.SSLMode;
import io.r2dbc.spi.ConnectionFactory;
import mubot.database.cache.GuildCache;
import mubot.database.cache.PrefixCache;
import org.springframework.r2dbc.core.DatabaseClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

public class DatabaseManager {
    // for heroku DATABASE_URL is stored as
    // postgres://<username>:<password>@<host>/<dbname>
    private final static String DB_URL = System.getenv("DATABASE_URL");
    private static DatabaseManager instance;
    private static PrefixCache prefixCache;
    private static GuildCache guildCache;
    private final String TABLE_NAME = "guilds";
    private String MAX_CONNECTIONS = System.getenv("DATABASE_MAX_CONNECTIONS");
    private DatabaseClient databaseClient;

    private DatabaseManager() {
        try {
            URI dbUri = new URI(DB_URL);

            if (MAX_CONNECTIONS == null) {
                MAX_CONNECTIONS = "10";
            }

            ConnectionFactory factory = new PostgresqlConnectionFactory(PostgresqlConnectionConfiguration.builder()
                    .host(dbUri.getHost()).port(dbUri.getPort()).username(dbUri.getUserInfo().split(":")[0])
                    .password(dbUri.getUserInfo().split(":")[1]).database(dbUri.getPath().replaceFirst("/", ""))
                    .enableSsl().sslMode(SSLMode.REQUIRE).build());

            ConnectionPool poolConfig = new ConnectionPool(ConnectionPoolConfiguration.builder(factory).initialSize(1)
                    .maxIdleTime(Duration.ofSeconds(30)).maxSize(Integer.parseInt(MAX_CONNECTIONS)).build());

            databaseClient = DatabaseClient.create(poolConfig);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void create() {
        if (instance == null) {
            instance = new DatabaseManager();
            prefixCache = new PrefixCache(instance);
            guildCache = new GuildCache(instance);
        }
    }

    public static String getTableName() {
        return DatabaseManager.instance.TABLE_NAME;
    }

    public static DatabaseManager getInstance() {
        return DatabaseManager.instance;
    }

    public DatabaseClient getClient() {
        return databaseClient;
    }

    public PrefixCache getPrefixCache() {
        return prefixCache;
    }

    public GuildCache getGuildCache() {
        return guildCache;
    }
}