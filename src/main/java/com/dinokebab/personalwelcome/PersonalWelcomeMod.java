package com.dinokebab.personalwelcome;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersonalWelcomeMod implements DedicatedServerModInitializer {
    public static final String MOD_ID = "personalwelcome";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static PersonalWelcomeConfig config;

    @Override
    public void onInitializeServer() {
        config = PersonalWelcomeConfig.load();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            server.execute(() -> sendWelcomeMessage(player));
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("myping").executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                int ping = player.latency;
                player.sendSystemMessage(Component.literal(color("&7Il tuo ping: &f" + ping + " &7ms")));
                return 1;
            }));

            dispatcher.register(Commands.literal("personalwelcome")
                    .then(Commands.literal("reload")
                            .requires(source -> source.getEntity() == null)
                            .executes(context -> {
                                reloadConfig();
                                context.getSource().sendSuccess(
                                        () -> Component.literal("Personal Welcome config reloaded."), true);
                                return 1;
                            })));
        });

        LOGGER.info("Personal Welcome initialized.");
    }

    private static void sendWelcomeMessage(ServerPlayer player) {
        if (!config.welcomeEnabled || player == null) return;

        String message = config.welcomeMessage
                .replace("%player%", player.getName().getString())
                .replace("%ping%", String.valueOf(player.latency))
                .replace("%online%", String.valueOf(player.server.getPlayerCount()))
                .replace("%max_players%", String.valueOf(player.server.getMaxPlayers()))
                .replace("%server_address%", config.serverAddress);

        for (String line : message.split("\\\\n|\n")) {
            if (!line.isBlank()) {
                player.sendSystemMessage(Component.literal(color(line)));
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
