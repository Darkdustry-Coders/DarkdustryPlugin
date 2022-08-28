package darkdustry.utils;

import arc.func.*;
import arc.math.Mathf;
import arc.struct.Seq;
import mindustry.gen.*;
import mindustry.io.SaveIO;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static arc.util.Strings.format;
import static arc.util.Strings.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.format;
import static darkdustry.components.Bundle.*;
import static darkdustry.discord.Bot.*;
import static java.util.Objects.requireNonNull;
import static mindustry.Vars.*;

// Страшно, но очень полезно.
// (C) xzxADIxzx, 2023 год
public class PageIterator {

    // region Client

    public static void commands(String[] args, Player player) {
        var locale = Find.locale(player.locale);
        client(args, player, clientCommands.getCommandList(), "help",
                (builder, i, command) -> builder
                        .append("\n[orange] ").append(clientCommands.getPrefix()).append(command.text).append("[white] ")
                        .append(get("commands." + command.text + ".params", command.paramText, locale)).append("[lightgray] - ")
                        .append(get("commands." + command.text + ".description", command.description, locale)), null);
    }

    public static void players(String[] args, Player player) {
        client(args, player, Groups.player.copy(new Seq<>()), "players",
                (builder, i, p) -> builder
                        .append("\n[#9c88ee]* [white]").append(p.admin ? "[\uE82C] " : "[\uE872] ").append(p.coloredName())
                        .append(" [lightgray]|[accent] ID: ").append(p.id)
                        .append(" [lightgray]|[accent] Locale: ").append(p.locale), null);
    }

    public static void maps(String[] args, Player player) {
        client(args, player, maps.customMaps(), "maps",
                (builder, i, map) -> builder.append("\n[lightgray] ").append(i + 1).append(". [orange]").append(map.name()),
                result -> result.append(format("commands.maps.current", Find.locale(player.locale), state.map.name())));
    }

    public static void saves(String[] args, Player player) {
        client(args, player, Seq.with(saveDirectory.list()).filter(SaveIO::isSaveValid), "saves",
                (builder, i, save) -> builder.append("\n[lightgray] ").append(i + 1).append(". [orange]").append(save.nameWithoutExtension()), null);
    }

    private static <T> void client(
            String[] args, Player player, Seq<T> content, String command,
            Cons3<StringBuilder, Integer, T> cons, Cons<StringBuilder> result) {

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
            cons.get(builder, i, content.get(i));

        if (result != null) result.get(builder);
        player.sendMessage(builder.toString());
    }

    // endregion
    // region Discord

    public static void players(SlashCommandInteractionEvent event) {
        discord(event, Groups.player.copy(new Seq<>()),
                size -> format(":bar_chart: Игроков на сервере: @", size),
                (builder, i, p) -> builder.append("`").append(p.admin ? "\uD83D\uDFE5" : "\uD83D\uDFE7").append(" ").append(p.id).append("` | ").append(p.plainName()).append("\n")
        );
    }

    public static void maps(SlashCommandInteractionEvent event) {
        discord(event, maps.customMaps(),
                size -> format(":map: Карт в плейлисте сервера: @", size),
                (builder, i, map) -> builder.append("**").append(i + 1).append(".** ").append(stripColors(map.name())).append(" (").append(map.width).append("x").append(map.height).append(")\n")
        );
    }

    private static <T> void discord(
            SlashCommandInteractionEvent event, Seq<T> content,
            Func<Integer, String> header, Cons3<StringBuilder, Integer, T> cons) {

        int page = event.getOption("page") != null ? requireNonNull(event.getOption("page")).getAsInt() : 1, pages = Math.max(1, Mathf.ceil(content.size / (float) maxPerPage));

        if (page > pages || page <= 0) {
            event.replyEmbeds(error(":interrobang: Неверная страница.", "Страница должна быть числом от 1 до @", pages).build()).queue();
            return;
        }

        var builder = new StringBuilder();
        for (int i = maxPerPage * (page - 1); i < Math.min(maxPerPage * page, content.size); i++)
            cons.get(builder, i, content.get(i));

        event.replyEmbeds(neutral(header.get(content.size))
                .setDescription(builder.toString())
                .setFooter(format("Страница @ / @", page, pages)).build()).queue();
    }

    // endregion
}
