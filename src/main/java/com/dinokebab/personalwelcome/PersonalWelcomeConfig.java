package com.dinokebab.personalwelcome;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PersonalWelcomeConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean welcomeEnabled = true;
    public String serverAddress = "dinokebab.duckdns.org";
    public String welcomeMessage = """
            &8━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            &b        MINECRAFT SERVER
            
            &aBenvenuto, &f%player%&a!
            
            &7%server_address%
            &7Giocatori online: &f%online%&7 / &f%max_players%
            &7Il tuo ping: &f%ping% &7ms
            
            &7Buona esplorazione!
            &8━━━━━━━━━━━━━━━━━━━━━━━━━━━━""";

    public static PersonalWelcomeConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("personal-welcome.json");
        try {
            if (Files.exists(path)) {
                return GSON.fromJson(Files.readString(path), PersonalWelcomeConfig.class);
            }
            PersonalWelcomeConfig config = new PersonalWelcomeConfig();
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(config));
            return config;
        } catch (IOException e) {
            throw new RuntimeException("Unable to load personal-welcome.json", e);
        }
    }

    public static void save(PersonalWelcomeConfig config) {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("personal-welcome.json");
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(config));
        } catch (IOException e) {
            throw new RuntimeException("Unable to save personal-welcome.json", e);
        }
    }
}
