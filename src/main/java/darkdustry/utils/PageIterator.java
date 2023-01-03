package darkdustry.utils;

import arc.func.*;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Strings;
import darkdustry.discord.Bot.Context;
import darkdustry.features.menus.MenuHandler;
import mindustry.gen.*;
import net.dv8tion.jda.api.EmbedBuilder;
import useful.Bundle;

import static darkdustry.PluginVars.*;
import static darkdustry.utils.Utils.*;
import static useful.Bundle.bundled;

// Страшно, но очень полезно.
// (C) xzxADIxzx, 2023 год
public class PageIterator {

    // region client

    public static void commands(String[] args, Player player) {
        client("help", args, player, getAvailableCommands(player), (builder, index, command) -> {
            var params = Bundle.get("commands." + command.text + ".params", command.paramText, player);
            var description = Bundle.get("commands." + command.text + ".description", command.description, player);

            builder.append(Bundle.format("commands.help.command", player, clientCommands.prefix, command.text, params, description));
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
        int page = args.length > 0 ? Strings.parseInt(args[0]) : 1, pages = Math.max(1, Mathf.ceil(content.size / (float) maxPerPage));
        if (page > pages || page < 1) {
            bundled(player, "commands.invalid-page", pages);
            return;
        }

        MenuHandler.showListMenu(player, "commands." + command + ".title", content, page, pages, cons);
    }

    // endregion
    // region discord

    public static void players(String[] args, Context context) {
        discord(args, context, Groups.player.copy(new Seq<>()),
                size -> Strings.format(":bar_chart: Players online: @", size),
                (builder, i, p) -> builder.append("`").append(p.admin ? "\uD83D\uDFE5" : "\uD83D\uDFE7").append(" #").append(p.id).append("`   ").append(p.plainName())
        );
    }

    public static void maps(String[] args, Context context) {
        discord(args, context, getAvailableMaps(),
                size -> Strings.format(":map: Maps in playlist: @", size),
                (builder, i, map) -> builder.append("**").append(i + 1).append(".** ").append(Strings.stripColors(map.name())).append(" (").append(map.width).append("x").append(map.height).append(")")
        );
    }

    private static <T> void discord(String[] args, Context context, Seq<T> content, Func<Integer, String> title, Cons3<StringBuilder, Integer, T> cons) {
        int page = args.length > 0 ? Strings.parseInt(args[0]) : 1, pages = Math.max(1, Mathf.ceil(content.size / (float) maxPerPage));
        if (page > pages || page < 1) {
            context.error(":interrobang: Page must be a number between 1 and @.", pages).queue();
            return;
        }

        context.message().replyEmbeds(new EmbedBuilder()
                .setColor(-13855508)
                .setTitle(title.get(content.size))
                .setDescription(formatList(content, page, cons))
                .setFooter(Strings.format("Page @ / @", page, pages)).build()).queue();
    }

    // endregion
}