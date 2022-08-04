package rewrite.commands;

import arc.util.CommandHandler;
import mindustry.gen.Player;
import rewrite.components.Config.Gamemode;
import rewrite.discord.Bot;

import java.util.Locale;

import static rewrite.PluginVars.*;
import static rewrite.components.Bundle.*;
import static rewrite.utils.Checks.*;

public class ClientCommands extends Commands<Player> {

    public ClientCommands(CommandHandler handler, Locale locale) {
        super(handler, locale);

        register("help", (args, player) -> {

        });
        register("discord", (args, player) -> {

        });
        register("t", (args, player) -> {

        });
        register("sync", (args, player) -> {

        });
        register("tr", (args, player) -> {

        });
        register("stats", (args, player) -> {

        });
        register("rank", (args, player) -> {

        });
        register("players", (args, player) -> {

        });
        register("hub", (args, player) -> {

        });
        register("vote", (args, player) -> {

        });
        register("votekick", (args, player) -> {

        });

        if (!config.mode.isDefault()) return;

        register("rtv", (args, player) -> {

        });
        register("vnw", (args, player) -> {

        });
        register("nominate", (args, player) -> {

        });
        register("maps", (args, player) -> {

        });
        register("saves", (args, player) -> {

        });
        register("history", (args, player) -> {

        });
        register("alerts", (args, player) -> {

        });
        register("login", (args, player) -> {
            if (isAdmin(player) || isCooldowned(player, "login")) return;

            Bot.sendMessageToAdmin(player);
            bundled(player, "commands.login.sent");
        });
    }
}
