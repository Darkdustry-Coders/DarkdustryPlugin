package darkdustry.utils;

import arc.func.*;
import arc.math.Mathf;
import arc.struct.Seq;
import darkdustry.database.Cache;
import darkdustry.discord.MessageContext;
import darkdustry.features.net.Socket;
import darkdustry.features.menus.MenuHandler;
import darkdustry.listeners.SocketEvents.*;
import discord4j.core.object.component.*;
import discord4j.core.spec.EmbedCreateSpec.Builder;
import mindustry.gen.*;
import useful.Bundle;

import static arc.util.Strings.*;
import static darkdustry.PluginVars.*;
import static darkdustry.utils.Utils.*;
import static discord4j.rest.util.Color.*;

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

    public static void maps(String[] args, Player player) {
        client(args, player, "maps", Utils::availableMaps, (builder, index, map) ->
                builder.append(Bundle.format("commands.maps.map", player, index, map.name(), map.author(), map.width, map.height)));
    }

    public static void saves(String[] args, Player player) {
        client(args, player, "saves", Utils::availableSaves, (builder, index, save) ->
                builder.append(Bundle.format("commands.saves.save", player, index, save.nameWithoutExtension(), Bundle.formatDateTime(player, save.lastModified()))));
    }

    public static void players(String[] args, Player player) {
        client(args, player, "players", () -> Groups.player.copy(new Seq<>()), (builder, index, other) ->
                builder.append(Bundle.format("commands.players.player", player, other.coloredName(), other.admin ? "\uE82C" : "\uE872", Cache.get(other).id, other.locale)));
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

    public static void maps(String[] args, MessageContext context) {
        discord(args, context, "maps", PageIterator::formatMapsPage);
    }

    public static void players(String[] args, MessageContext context) {
        discord(args, context, "players", PageIterator::formatPlayersPage);
    }

    private static void discord(String[] args, MessageContext context, String type, Cons2<Builder, ListResponse> formatter) {
        var server = args[0];
        if (Checks.notFound(context, server)) return;

        Socket.request(new ListRequest(type, server, 1), response -> context
                        .reply(embed -> formatter.get(embed, response))
                        .withComponents(createPageButtons(type, server, response))
                        .subscribe(), context::timeout);
    }

    public static void formatMapsPage(Builder embed, ListResponse response) {
        formatDiscordPage(embed, "Maps in Playlist: @", "Page @ / @", response);
    }

    public static void formatPlayersPage(Builder embed, ListResponse response) {
        formatDiscordPage(embed, "Players Online: @", "Page @ / @", response);
    }

    public static void formatDiscordPage(Builder embed, String title, String footer, ListResponse response) {
        embed.title(format(title, response.total));
        embed.footer(format(footer, response.page, response.pages), null);

        embed.color(SUMMER_SKY);
        embed.description(response.content);
    }

    public static ActionRow createPageButtons(String type, String server, ListResponse response) {
        return ActionRow.of(
                Button.primary(type + "-" + server + "-" + (response.page - 1), "<--").disabled(response.page <= 1),
                Button.primary(type + "-" + server + "-" + (response.page + 1), "-->").disabled(response.page >= response.pages)
        );
    }

    // endregion
}