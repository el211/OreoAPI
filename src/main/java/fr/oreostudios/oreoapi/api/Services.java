// File: src/main/java/fr/oreostudios/oreoapi/api/Services.java
package fr.oreostudios.oreoapi.api;

import fr.oreostudios.oreoapi.OreoApi;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;

public final class Services {
    private Services() {}

    public static void register(Plugin plugin, OreoApi api) {
        ServicesManager sm = Bukkit.getServicesManager();
        sm.register(OreoApi.class, api, plugin, ServicePriority.Normal);
    }

    public static OreoApi resolve() {
        var reg = Bukkit.getServicesManager().getRegistration(OreoApi.class);
        if (reg == null) throw new IllegalStateException("OreoApi not registered in ServicesManager.");
        return reg.getProvider();
    }
}
