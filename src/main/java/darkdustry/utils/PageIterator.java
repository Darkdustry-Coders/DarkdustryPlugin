package darkdustry.utils;

import arc.func.*;
import arc.math.Mathf;
import arc.struct.Seq;
import darkdustry.discord.MessageContext;
import darkdustry.features.menus.MenuHandler;
import mindustry.gen.*;
import useful.Bundle;

import static arc.util.Strings.*;
import static darkdustry.PluginVars.*;
import static darkdustry.utils.Utils.*;

// Страшно, но очень полезно.
// (C) xzxADIxzx, 2023 год
public class PageIterator {

    // region client

    public static void commands(String[] args, Player player) {
        client(args, player, "help", availableCommands(player), (builder, index, command) -> {
            var params = Bundle.get("commands." + command.text + ".params", command.paramText, player);
            var description = Bundle.get("commands." + command.text + ".description", command.description, player);

            builder.append(Bundle.format("commands.help.command", player, command.text, params, description));
        });
    }

    public static void players(String[] args, Player player) {
        client(args, player, "players", Groups.player.copy(new Seq<>()), (builder, index, p) ->
                builder.append(Bundle.format("commands.players.player", player, p.coloredName(), p.admin ? "\uE82C" : "\uE872", p.id, p.locale)));
    }

    public static void maps(String[] args, Player player) {
        client(args, player, "maps", availableMaps(), (builder, index, map) ->
                builder.append(Bundle.format("commands.maps.map", player, index, map.name(), map.author(), map.width, map.height)));
    }

    public static void saves(String[] args, Player player) {
        client(args, player, "saves", availableSaves(), (builder, index, save) ->
                builder.append(Bundle.format("commands.saves.save", player, index, save.nameWithoutExtension(), formatDateTime(save.lastModified()))));
    }

    private static <T> void client(String[] args, Player player, String command, Seq<T> content, Cons3<StringBuilder, Integer, T> cons) {
        int page = args.length > 0 ? parseInt(args[0]) : 1, pages = Math.max(1, Mathf.ceil(content.size / (float) maxPerPage));
        if (page > pages || page < 1) {
            Bundle.send(player, "commands.invalid-page", pages);
            return;
        }

        MenuHandler.showListMenu(player, page, pages, "commands." + command + ".title", content, cons);
    }

    // endregion
    // region discord

    public static void players(String[] args, MessageContext context) {
        discord(args, context,
                "Players Online: @",
                "Page @ / @",

                Groups.player.copy(new Seq<>()),

                (player, index) -> format("**@.** @", index, player.plainName()),
                (player) -> format("ID: #@\nLocale: @", player.id, player.locale)
        );
    }

    public static void maps(String[] args, MessageContext context) {
        discord(args, context,
                "Maps in Playlist: @",
                "Page @ / @",

                availableMaps(),

                (map, index) -> format("**@.** @", index, map.plainName()),
                (map) -> format("Author: @\nSize: @x@", map.plainAuthor(), map.width, map.height)
        );
    }

    private static <T> void discord(String[] args, MessageContext context, String title, String footer, Seq<T> values, Func2<T, Integer, String> fieldName, Func<T, String> fieldValue) {
        int page = args.length > 0 ? parseInt(args[0]) : 1, pages = Math.max(1, Mathf.ceil(values.size / (float) maxPerPage));
        if (page > pages || page < 1) {
            context.error("Invalid Page", "Page must be a number between 1 and @.", pages).subscribe();
            return;
        }

        context.info(embed -> {
            embed.title(format(title, values.size));
            embed.footer(format(footer, page, pages), null);

            for (int index = maxPerPage * (page - 1); index < Math.min(maxPerPage * page, values.size); index++) {
                var value = values.get(index);
                embed.addField(fieldName.get(value, index + 1), fieldValue.get(value), false);
            }
        }).subscribe();
    }

    // endregion
}