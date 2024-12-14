package org.strassburger.lifestealz.util.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.strassburger.lifestealz.LifeStealZ;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MariaDBStorage extends SQLStorage {
    public MariaDBStorage(LifeStealZ plugin) {
        super(plugin);
    }

    Connection createConnection() {
        FileConfiguration config = getPlugin().getConfigManager().getStorageConfig();

        final String HOST = config.getString("host");
        final String PORT = config.getString("port");
        final String DATABASE = config.getString("database");
        final String USERNAME = config.getString("username");
        final String PASSWORD = config.getString("password");

        try {
            return DriverManager.getConnection("jdbc:mariadb://" + HOST + ":" + PORT + "/" + DATABASE, USERNAME, PASSWORD);
        } catch (SQLException e) {
            getPlugin().getLogger().severe("Failed to create connection to MariaDB database: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void save(PlayerData playerData) {
        try (Connection connection = createConnection()) {
            if (connection == null) return;
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO hearts (uuid, name, maxhp, hasbeenRevived, craftedHearts, craftedRevives, killedOtherPlayers, firstJoin) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE name = VALUES(name), maxhp = VALUES(maxhp), hasbeenRevived = VALUES(hasbeenRevived), " +
                            "craftedHearts = VALUES(craftedHearts), craftedRevives = VALUES(craftedRevives), killedOtherPlayers = VALUES(killedOtherPlayers)"
            )) {
                statement.setString(1, playerData.getUuid());
                statement.setString(2, playerData.getName());
                statement.setDouble(3, playerData.getMaxHealth());
                statement.setInt(4, playerData.getHasbeenRevived());
                statement.setInt(5, playerData.getCraftedHearts());
                statement.setInt(6, playerData.getCraftedRevives());
                statement.setInt(7, playerData.getKilledOtherPlayers());
                statement.setLong(8, playerData.getFirstJoin());

                statement.executeUpdate();
            } catch (SQLException e) {
                getPlugin().getLogger().severe("Failed to save player data to MariaDB database: " + e.getMessage());
            }
        } catch (SQLException e) {
            getPlugin().getLogger().severe("Failed to save player data to MariaDB database: " + e.getMessage());
        }
    }
}
