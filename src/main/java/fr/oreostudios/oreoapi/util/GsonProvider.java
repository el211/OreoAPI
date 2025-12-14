package fr.oreostudios.oreoapi.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class GsonProvider {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final Gson GSON_PRETTY = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    private GsonProvider() {}

    public static Gson gson(boolean pretty) {
        return pretty ? GSON_PRETTY : GSON;
    }
}
