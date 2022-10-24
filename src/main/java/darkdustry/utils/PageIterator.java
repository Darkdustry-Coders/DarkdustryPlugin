package darkdustry.utils;

import arc.func.*;
import arc.math.Mathf;
import arc.struct.Seq;
import darkdustry.discord.Bot.Context;
import mindustry.gen.*;
import mindustry.io.SaveIO;
import net.dv8tion.jda.api.EmbedBuilder;

import static arc.util.Strings.format;
import static arc.util.Strings.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.format;
import static darkdustry.components.Bundle.*;
import static mindustry.Vars.*;

// Страшно, но очень полезно.
// (C) xzxADIxzx, 2023 год
public class PageIterator {

    // region Client

    public static void commands(String[] args, Player player) {
        var locale = Find.locale(player.locale);
        client(args, player, clientCommands.getCommandList().select(command -> !hiddenCommands.contains(command.text) && (player.admin || !adminOnlyCommands.contains(command.text))), "help",
                (builder, i, command) -> builder
                        .append("  [orange]").append(clientCommands.getPrefix()).append(command.text).append("[white] ")
                        .append(get("commands." + command.text + ".params", command.paramText, locale))
                        .append("[gray] - [lightgray]")
                        .append(get("commands." + command.text + ".description", command.description, locale)));
    }

    public static void players(String[] args, Player player) {
        var locale = Find.locale(player.locale);
        client(args, player, Groups.player.copy(new Seq<>()), "players",
                (builder, i, p) -> builder.append("  ").append(format("commands.players.player", locale, p.coloredName(), p.admin ? "\uE82C" : "\uE872", p.id, p.locale)));
    }

    public static void maps(String[] args, Player player) {
        var locale = Find.locale(player.locale);
        client(args, player, maps.customMaps(), "maps",
                (builder, i, map) -> builder.append("[orange]").append(i + 1).append("[lightgray] - [accent]").append(map.name()).append(map == state.map ? get("commands.maps.current", locale) : ""));
    }

    public static void saves(String[] args, Player player) {
        client(args, player, saveDirectory.seq().filter(SaveIO::isSaveValid), "saves",
                (builder, i, save) -> builder.append("[orange]").append(i + 1).append("[lightgray] - [accent]").append(save.nameWithoutExtension()));
    }

    private static <T> void client(String[] args, Player player, Seq<T> content, String command, Cons3<StringBuilder, Integer, T> cons) {
        if (args.length > 0 && !canParseInt(args[0])) {
            bundled(player, "commands.page-not-int");
            return;
        }

        int page = args.length > 0 ? parseInt(args[0]) : 1, pages = Math.max(1, Mathf.ceil(content.size / (float) maxPerPage));

        if (page > pages || page <= 0) {
            bundled(player, "commands.under-page", pages);
            return;
        }

        var builder = new StringBuilder(format("commands." + command + ".page", Find.locale(player.locale), page, pages));
        for (int i = maxPerPage * (page - 1); i < Math.min(maxPerPage * page, content.size); i++)
            cons.get(builder.append("\n"), i, content.get(i));

        player.sendMessage(builder.toString());
    }

    // endregion
    // region Discord

    public static void players(String[] args, Context context) {
        discord(args, context, Groups.player.copy(new Seq<>()),
                content -> format(":bar_chart: Players online: @", content.size),
                (builder, i, p) -> builder.append("`").append(p.admin ? "\uD83D\uDFE5" : "\uD83D\uDFE7").append(" #").append(p.id).append("`   ").append(p.plainName())
        );
    }

    public static void maps(String[] args, Context context) {
        discord(args, context, maps.customMaps(),
                content -> format(":map: Maps in playlist: @", content.size),
                (builder, i, map) -> builder.append("**").append(i + 1).append(".** ").append(stripColors(map.name())).append(" (").append(map.width).append("x").append(map.height).append(")")
        );
    }

    private static <T> void discord(String[] args, Context context, Seq<T> content, Func<Seq<T>, String> title, Cons3<StringBuilder, Integer, T> cons) {
        int page = args.length > 0 ? parseInt(args[0]) : 1, pages = Math.max(1, Mathf.ceil(content.size / (float) maxPerPage));

        if (page > pages || page <= 0) {
            context.error(":interrobang: Page must be a number between 1 and @.", pages).queue();
            return;
        }

        var builder = new StringBuilder();
        for (int i = maxPerPage * (page - 1); i < Math.min(maxPerPage * page, content.size); i++)
            cons.get(builder.append("\n"), i, content.get(i));

        context.message().replyEmbeds(new EmbedBuilder()
                .setColor(-13855508)
                .setTitle(title.get(content))
                .setDescription(builder.toString())
                .setFooter(format("Page @ / @", page, pages)).build()).queue();
    }

    // endregion
}