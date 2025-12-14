package fr.oreostudios.oreoapi.api;

import fr.oreostudios.oreoapi.OreoAPIPlugin;
import fr.oreostudios.oreoapi.OreoApi;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class OreoApiProvider {
    private OreoApiProvider() {}

    public static OreoApi get() {
        Plugin p = Bukkit.getPluginManager().getPlugin("OreoAPI");
        if (!(p instanceof OreoAPIPlugin apiPlugin)) {
            throw new IllegalStateException("OreoAPI plugin not found or not enabled.");
        }
        return apiPlugin.getApi();
    }
}
