package darkdustry.utils;

import arc.func.Cons3;
import arc.math.Mathf;
import arc.struct.Seq;
import darkdustry.discord.MessageContext;
import darkdustry.features.menus.MenuHandler;
import mindustry.gen.*;
import useful.Bundle;

import static arc.util.Strings.*;
import static darkdustry.PluginVars.maxPerPage;
import static darkdustry.utils.Utils.*;

// Страшно, но очень полезно.
// (C) xzxADIxzx, 2023 год
public class PageIterator {

    // region client

    public static void commands(String[] args, Player player) {
        client("help", args, player, getAvailableCommands(player), (builder, index, command) -> {
            var params = Bundle.get("commands." + command.text + ".params", command.paramText, player);
            var description = Bundle.get("commands." + command.text + ".description", command.description, player);

            builder.append(Bundle.format("commands.help.command", player, command.text, params, description));
        });
    }

    public static void players(String[] args, Player player) {
        client("players", args, player, Groups.player.copy(new Seq<>()), (builder, index, p) ->
                builder.append(Bundle.format("commands.players.player", player, p.coloredName(), p.admin ? "\uE82C" : "\uE872", p.id, p.locale)));
    }

    public static void maps(String[] args, Player player) {
        client("maps", args, player, getAvailableMaps(), (builder, index, map) ->
                builder.append(Bundle.format("commands.maps.map", player, index + 1, map.name(), map.author(), map.width, map.height)));
    }

    public static void saves(String[] args, Player player) {
        client("saves", args, player, getAvailableSaves(), (builder, index, save) ->
                builder.append(Bundle.format("commands.saves.save", player, index + 1, save.nameWithoutExtension(), formatLongDate(save.lastModified()))));
    }

    private static <T> void client(String command, String[] args, Player player, Seq<T> content, Cons3<StringBuilder, Integer, T> cons) {
        int page = args.length > 0 ? parseInt(args[0]) : 1, pages = Math.max(1, Mathf.ceil(content.size / (float) maxPerPage));
        if (page > pages || page < 1) {
            Bundle.send(player, "commands.invalid-page", pages);
            return;
        }

        MenuHandler.showListMenu(player, "commands." + command + ".title", content, page, pages, cons);
    }

    // endregion
    // region discord

    public static void players(String[] args, MessageContext context) {
        discord("Players Online: ", args, context, Groups.player.copy(new Seq<>()), (builder, i, player) ->
                builder.append("**").append(i + 1).append(".** ").append(player.plainName()).append(" **|** ID: #").append(player.id).append(" **|** Locale: ").append(player.locale));
    }

    public static void maps(String[] args, MessageContext context) {
        discord("Maps in Playlist: ", args, context, getAvailableMaps(), (builder, i, map) ->
                builder.append("**").append(i + 1).append(".** ").append(stripColors(map.name())).append(" by ").append(stripColors(map.author())).append("\n").append(map.width).append("x").append(map.height));
    }

    private static <T> void discord(String title, String[] args, MessageContext context, Seq<T> content, Cons3<StringBuilder, Integer, T> cons) {
        int page = args.length > 0 ? parseInt(args[0]) : 1, pages = Math.max(1, Mathf.ceil(content.size / (float) maxPerPage));
        if (page > pages || page < 1) {
            context.error("Invalid Page", "Page must be a number between 1 and @.", pages).subscribe();
            return;
        }

        context.info(embed -> embed
                .title(title + content.size)
                .description(formatList(content, page, cons))
                .footer("Page " + page + " / " + pages, null)).subscribe();
    }

    // endregion
}