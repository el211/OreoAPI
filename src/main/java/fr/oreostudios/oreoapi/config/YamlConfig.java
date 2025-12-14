package fr.oreostudios.oreoapi.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class YamlConfig {
    private final JavaPlugin plugin;

    public YamlConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void ensureDefaultConfig() {
        plugin.saveDefaultConfig();
    }

    public void reload() {
        plugin.reloadConfig();
    }

    public FileConfiguration raw() {
        return plugin.getConfig();
    }
}
