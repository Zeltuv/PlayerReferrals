package com.stanexe.playerreferrals;

import com.stanexe.playerreferrals.commands.ReferralAdminCommand;
import com.stanexe.playerreferrals.commands.ReferralCommand;
import com.stanexe.playerreferrals.commands.ReferralLeaderboardCommand;
import com.stanexe.playerreferrals.events.JoinListener;
import com.stanexe.playerreferrals.util.Cache;
import com.stanexe.playerreferrals.util.Milestones;
import com.stanexe.playerreferrals.util.PlayerReferralsExpansion;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public final class PlayerReferrals extends JavaPlugin {

    public static PlatformScheduler getScheduler() {
        return foliaLib.getScheduler();
    }

    private static FoliaLib foliaLib;

    private static PlayerReferrals instance;
    private FileConfiguration messagesConfig;

    public static PlayerReferrals getInstance() {
        return instance;
    }
    @Override
    public void onEnable() {
        instance = this;
        foliaLib = new FoliaLib(this);

        // Save default configs
        this.saveDefaultConfig();
        createMessagesConfig();

        // Init milestones
        new Milestones();
        new Cache();

        // Events
        Bukkit.getPluginManager().registerEvents(new JoinListener(), this);

        // Commands
        Objects.requireNonNull(getCommand("referraladmin")).setExecutor(new ReferralAdminCommand());
        Objects.requireNonNull(getCommand("referral")).setExecutor(new ReferralCommand());
            Objects.requireNonNull(getCommand("referralleaderboard")).setExecutor(new ReferralLeaderboardCommand());
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            new PlayerReferralsExpansion().register();
        }

    }


    @Override
    public void onDisable() {
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void createMessagesConfig() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            messagesFile.getParentFile().mkdirs();
            saveResource("messages.yml", false);
        }
        messagesConfig = new YamlConfiguration();
        try {
            messagesConfig.load(messagesFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public void reloadMessagesConfig() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        messagesConfig = new YamlConfiguration();
        try {
            messagesConfig.load(messagesFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }
}
