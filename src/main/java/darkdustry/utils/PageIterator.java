package darkdustry.utils;

import arc.func.*;
import arc.math.Mathf;
import arc.struct.Seq;
import darkdustry.components.Cache;
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
        client(args, player, "help", () -> availableCommands(player), (builder, index, command) -> {
            var params = Bundle.get("commands." + command.text + ".params", command.paramText, player);
            var description = Bundle.get("commands." + command.text + ".description", command.description, player);

            builder.append(Bundle.format("commands.help.command", player, command.text, params, description));
        });
    }

    public static void players(String[] args, Player player) {
        client(args, player, "players", () -> Groups.player.copy(new Seq<>()), (builder, index, other) ->
                builder.append(Bundle.format("commands.players.player", player, other.coloredName(), other.admin ? "\uE82C" : "\uE872", Cache.get(other).id, other.locale)));
    }

    public static void maps(String[] args, Player player) {
        client(args, player, "maps", Utils::availableMaps, (builder, index, map) ->
                builder.append(Bundle.format("commands.maps.map", player, index, map.name(), map.author(), map.width, map.height)));
    }

    public static void saves(String[] args, Player player) {
        client(args, player, "saves", Utils::availableSaves, (builder, index, save) ->
                builder.append(Bundle.format("commands.saves.save", player, index, save.nameWithoutExtension(), Bundle.formatDateTime(player, save.lastModified()))));
    }

    private static <T> void client(String[] args, Player player, String name, Prov<Seq<T>> content, Cons3<StringBuilder, Integer, T> formatter) {
        int page = args.length > 0 ? parseInt(args[0]) : 1, pages = Math.max(1, Mathf.ceil((float) content.get().size / maxPerPage));
        if (page > pages || page < 1) {
            Bundle.send(player, "commands.invalid-page", pages);
            return;
        }

        MenuHandler.showListMenu(player, page, "commands." + name + ".title", content, formatter);
    }

    // endregion
    // region discord

    public static void players(String[] args, MessageContext context) {
        discord(args, context,
                "Players Online: @",
                "Page @ / @",

                Groups.player.copy(new Seq<>()),

                (other, index) -> format("**@.** @", index, other.plainName()),
                (other) -> format("ID: @\nLocale: @", Cache.get(other).id, other.locale)
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