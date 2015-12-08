package ninja.smirking.economy.storage;

import ninja.smirking.economy.EconomyData;
import ninja.smirking.economy.EconomyPlugin;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Connor Spencer Harries
 */
public class MysqlEconomyDataDao implements EconomyDataDao {
    private static final String UPSERT_STATEMENT = "INSERT INTO economy (uniqueId, balance) VALUES(?, ?) ON DUPLICATE KEY UPDATE balance = VALUES(balance)";

    private final Map<UUID, EconomyData> cache;
    private final HikariDataSource source;
    private final EconomyPlugin plugin;

    public MysqlEconomyDataDao(EconomyPlugin plugin) throws Exception {
        String hostname = plugin.getConfig().getString("hostname", "127.0.0.1");
        String username = plugin.getConfig().getString("username", "root");
        String database = plugin.getConfig().getString("database", username);
        String password = plugin.getConfig().getString("password", "");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:mysql://%s/%s", hostname, database));
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.setPassword(password);
        config.setUsername(username);

        this.source = new HikariDataSource(config);
        this.cache = Maps.newConcurrentMap();
        this.plugin = plugin;

        try (Connection connection = source.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS economy (" +
                "id INT AUTO_INCREMENT PRIMARY KEY NOT NULL," +
                "uniqueId CHAR(36) UNIQUE NOT NULL," +
                "balance DOUBLE DEFAULT 0" +
                ") ENGINE=InnoDB;"
            );
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (cache.size() < 1) {
                    return;
                }

                List<EconomyData> pending = Lists.newLinkedList();
                for (Iterator<EconomyData> iterator = cache.values().iterator(); iterator.hasNext(); ) {
                    EconomyData data = iterator.next();
                    if (data.isDirty()) {
                        pending.add(data);
                    }
                    if (plugin.getServer().getPlayer(data.getUniqueId()) == null) {
                        iterator.remove();
                    }
                }

                if (pending.size() == 1) {
                    save(pending.get(0));
                } else {
                    saveBatch(pending);
                }
            }
        }.runTaskTimerAsynchronously(plugin, EconomyPlugin.SAVE_INTERVAL / 2, EconomyPlugin.SAVE_INTERVAL);
    }

    @Override
    public boolean isLoaded(UUID uniqueId) {
        return cache.get(uniqueId) != null;
    }

    @Override
    public EconomyData get(UUID uniqueId) {
        return cache.computeIfAbsent(uniqueId, uuid -> {
            EconomyData result = null;
            try (Connection connection = source.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT balance FROM economy WHERE uniqueId = ?")) {
                statement.setString(1, uniqueId.toString());
                try (ResultSet set = statement.executeQuery()) {
                    if (set.next()) {
                        result = new EconomyData(uniqueId, set.getDouble(1));
                        result.setDirty(false);
                    } else {
                        result = new EconomyData(uniqueId, 0.0D);
                    }
                }
            } catch (SQLException cause) {
                StringWriter stringWriter = new StringWriter();
                PrintWriter writer = new PrintWriter(stringWriter);
                cause.printStackTrace(writer);
                plugin.getLogger().log(Level.SEVERE, "Failed to load EconomyData for {0}: {1}", new Object[]{
                    uniqueId,
                    stringWriter.toString()
                });
            }
            return result;
        });
    }

    @Override
    public void save(EconomyData data) {
        Preconditions.checkNotNull(data, "data should not be null");
        try (Connection connection = source.getConnection(); PreparedStatement statement = connection.prepareStatement(UPSERT_STATEMENT)) {
            statement.setString(1, data.getUniqueId().toString());
            statement.setDouble(2, data.getBalance());
            statement.executeUpdate();
            data.setDirty(false);
        } catch (SQLException cause) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save EconomyData for {0}", new Object[]{
                data.getUniqueId(),
                cause
            });
        }
    }

    @Override
    public void saveAll() {
        saveBatch(cache.values().stream().filter(EconomyData::isDirty).collect(Collectors.toList()));
    }

    @Override
    public void close() {
        saveAll();
        cache.clear();
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(HikariDataSource.class.getClassLoader());
            source.close();
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    private void saveBatch(List<EconomyData> queue) {
        if (queue.size() < 1) {
            return;
        }

        try (Connection connection = source.getConnection()) {
            for (List<EconomyData> chunk : Lists.partition(queue, 150)) {
                try (PreparedStatement statement = connection.prepareStatement(UPSERT_STATEMENT)) {
                    for (EconomyData data : chunk) {
                        statement.setString(1, data.getUniqueId().toString());
                        statement.setDouble(2, data.getBalance());
                        data.setDirty(false);
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
            }
        } catch (SQLException cause) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save all EconomyData: {0}", new Object[]{
                cause
            });
        }
    }
}
