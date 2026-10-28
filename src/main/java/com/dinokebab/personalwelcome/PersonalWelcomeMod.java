package com.dinokebab.personalwelcome;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersonalWelcomeMod implements DedicatedServerModInitializer {
    public static final String MOD_ID = "personalwelcome";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final int WELCOME_DELAY_TICKS = 20;

    private static final Map<UUID, Integer> pendingWelcomes =
            new HashMap<>();

    private static PersonalWelcomeConfig config;

    @Override
    public void onInitializeServer() {
        config = PersonalWelcomeConfig.load();

        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> {
                    pendingWelcomes.put(
                            handler.player.getUUID(),
                            WELCOME_DELAY_TICKS
                    );
                }
        );

        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> {
                    pendingWelcomes.remove(handler.player.getUUID());
                }
        );

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<Map.Entry<UUID, Integer>> iterator =
                    pendingWelcomes.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<UUID, Integer> entry = iterator.next();
                int remainingTicks = entry.getValue() - 1;

                if (remainingTicks > 0) {
                    entry.setValue(remainingTicks);
                    continue;
                }

                UUID playerId = entry.getKey();
                iterator.remove();

                ServerPlayer player =
                        server.getPlayerList().getPlayer(playerId);

                if (player != null) {
                    sendWelcomeMessage(server, player);
                }
            }
        });

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {
                    dispatcher.register(
                            Commands.literal("myping")
                                    .executes(context -> {
                                        ServerPlayer player = context
                                                .getSource()
                                                .getPlayerOrException();

                                        int ping = player.connection.latency();

                                        player.sendSystemMessage(
                                                Component.literal(
                                                        color(
                                                                "&7Il tuo ping: &f"
                                                                        + ping
                                                                        + " &7ms"
                                                        )
                                                )
                                        );

                                        return 1;
                                    })
                    );

                    dispatcher.register(
                            Commands.literal("personalwelcome")
                                    .then(
                                            Commands.literal("reload")
                                                    .requires(source ->
                                                            source.getEntity() == null
                                                    )
                                                    .executes(context -> {
                                                        reloadConfig();

                                                        context.getSource()
                                                                .sendSuccess(
                                                                        () -> Component.literal(
                                                                                "Personal Welcome config reloaded."
                                                                        ),
                                                                        true
                                                                );

                                                        return 1;
                                                    })
                                    )
                    );
                }
        );

        LOGGER.info("Personal Welcome initialized with delayed welcomes.");
    }

    private static void sendWelcomeMessage(
            MinecraftServer server,
            ServerPlayer player
    ) {
        if (player == null || !config.welcomeEnabled) {
            return;
        }

        int online = server.getPlayerList().getPlayers().size();
        int maximum = server.getMaxPlayers();
        int ping = player.connection.latency();

        for (String originalLine : config.welcomeMessage.split("\\\\n|\n")) {
            String line = originalLine;

            if (online == 1 && originalLine.contains("%online%")) {
                line = "&aSei l'unico utente online!";
            }

            line = line
                    .replace("%player%", player.getName().getString())
                    .replace("%ping%", String.valueOf(ping))
                    .replace("%online%", String.valueOf(online))
                    .replace("%max_players%", String.valueOf(maximum))
                    .replace("%server_address%", config.serverAddress);

            if (!line.isBlank()) {
                player.sendSystemMessage(
                        Component.literal(color(line))
                );
            }
        }
    }

    private static String color(String text) {
        return text.replace('&', '\u00a7');
    }

    public static void reloadConfig() {
        config = PersonalWelcomeConfig.load();
    }
}
