package com.stanexe.playerreferrals.util;

import com.stanexe.playerreferrals.PlayerReferrals;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseUtil {
    static final PlayerReferrals plugin = PlayerReferrals.getInstance();
    static final String dbType = plugin.getConfig().getString("database-type");
    private static final ExecutorService dbThread = Executors.newSingleThreadExecutor();
    private static Connection conn;

    public static String getDbType() {
        return dbType;
    }

    public static Connection getConn() {
        try {
            if (conn != null && conn.isValid(1)) {
                return conn;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (dbType == null) {
            plugin.getLogger().info("Invalid database type. Expected SQLITE or MYSQL, received nothing.");
            Bukkit.getPluginManager().disablePlugin(PlayerReferrals.getInstance());
        } else {
            if (dbType.equalsIgnoreCase("SQLITE")) {
                try {
                    conn = new SQLite().openConnection();
                    return conn;
                } catch (SQLException e) {
                    plugin.getLogger().warning("Unable to open connection to database. If this is a bug, please report it.");
                }
            } else if (dbType.equalsIgnoreCase("MYSQL")) {
                conn = new MySQL().openConnection();
                if (conn != null) {
                    plugin.getLogger().info("Connected to MYSQL database!");
                    return conn;
                }
            } else {
                plugin.getLogger().info("Invalid database type. Expected SQLITE or MYSQL, received: " + dbType);
                Bukkit.getPluginManager().disablePlugin(PlayerReferrals.getInstance());
            }
        }
        return null;
    }

    public static boolean initializeTables(Connection conn) {
        if (conn == null) {
            plugin.getLogger().warning("Connection to the database appears to be invalid.");
            PlayerReferrals.getScheduler().runNextTick(wrappedTask -> {
                Bukkit.getPluginManager().disablePlugin(plugin);
            });

            return false;
        }
        String tablePrefix = plugin.getConfig().getString("table-prefix");
        String[] sql = {"CREATE TABLE IF NOT EXISTS `" + tablePrefix + "referrals` (`uuid` CHAR(36) PRIMARY KEY NOT NULL, `referrer-uuid` CHAR(36));",
                "CREATE TABLE IF NOT EXISTS `" + tablePrefix + "referral-scores` (`uuid` CHAR(36) PRIMARY KEY NOT NULL, `score` INT DEFAULT 0 NOT NULL);",
                "CREATE TABLE IF NOT EXISTS `" + tablePrefix + "awaiting-reward` (`uuid` CHAR(36) NOT NULL, `reward-score` INT NOT NULL, `referral-uuid` CHAR(36) NOT NULL)",
                "CREATE TABLE IF NOT EXISTS `" + tablePrefix + "ip-addresses` (`uuid` CHAR(36) PRIMARY KEY NOT NULL, `ip` TEXT)"};
        int i;
        for (i = 0; i < sql.length; i++) {
            try {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(sql[i]);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;

    }

    public static List<RefUser> getTopPlayers() {
        Connection conn;
        String tablePrefix = plugin.getConfig().getString("table-prefix");
        try {
            conn = getConn();
            if (conn != null) {
                PreparedStatement stmt;
                stmt = conn.prepareStatement("SELECT uuid FROM `" + tablePrefix + "referral-scores` ORDER BY score DESC LIMIT 10;");
                ResultSet resultSet = stmt.executeQuery();
                List<RefUser> topPlayers = new ArrayList();
                while (resultSet.next()) {
                    UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                    topPlayers.add(new RefUser(uuid));
                }
                return topPlayers;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static ExecutorService getDbThread() {
        return dbThread;
    }

}
