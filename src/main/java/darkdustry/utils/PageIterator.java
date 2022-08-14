package darkdustry.utils;

import arc.func.*;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Strings;
import darkdustry.discord.SlashContext;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.io.SaveIO;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.util.Locale;

import static arc.util.Strings.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.*;
import static mindustry.Vars.*;

// Страшно, но очень полезно.
// (C) xzxADIxzx, 2023 год
public class PageIterator {

    // region Client

    public static void commands(String[] args, Player player) {
        Locale locale = Find.locale(player.locale);
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
                (builder, i, map) -> builder.append("\n[lightgray] ").append(i).append(". [orange]").append(map.name()),
                result -> result.append(format("commands.maps.current", Find.locale(player.locale), state.map.name())));
    }

    public static void saves(String[] args, Player player) {
        client(args, player, Seq.with(saveDirectory.list()).filter(SaveIO::isSaveValid), "saves",
                (builder, i, save) -> builder.append("\n[lightgray] ").append(i).append(". [orange]").append(save.nameWithoutExtension()), null);
    }

    private static <T> void client(
            String[] args, Player player, Seq<T> content, String command,
            Cons3<StringBuilder, Integer, T> cons, Cons<StringBuilder> result) {

        if (args.length > 0 && !canParseInt(args[0])) {
            bundled(player, "commands.page-not-int");
            return;
        }

        int page = args.length > 0 ? parseInt(args[0]) : 1, pages = Math.max(1, Mathf.ceil(content.size / 8f));

        if (page - 1 >= pages || page <= 0) {
            bundled(player, "commands.under-page", pages);
            return;
        }

        StringBuilder builder = new StringBuilder(format("commands." + command + ".page", Find.locale(player.locale), page, pages));
        for (int i = 8 * (page - 1); i < Math.min(8 * page, content.size); i++)
            cons.get(builder, i, content.get(i));

        if (result != null) result.get(builder);
        player.sendMessage(builder.toString());
    }

    // endregion
    // region Discord

    public static void players(SlashContext context) {
        discord(context, Groups.player.copy(new Seq<>()),
                (builder, p) -> builder.append(stripColors(p.name)).append(" (ID: ").append(p.id).append(")\n"),
                ":satellite: Всего игроков на сервере: @");
    }

    public static void maps(SlashContext context) {
        discord(context, maps.customMaps(),
                (builder, map) -> builder.append(stripColors(map.name())).append("\n"),
                ":map: Всего карт на сервере: @");
    }

    private static <T> void discord(
            SlashContext context, Seq<T> content,
            Cons2<StringBuilder, T> cons, String result) {

        int page = context.getOption("page") != null ? context.getOption("page").getAsInt() : 1, pages = Math.max(1, Mathf.ceil(Groups.player.size() / 16f));

        if (page - 1 >= pages || page <= 0) {
            context.error(":interrobang: Неверная страница.", "Страница должна быть числом от 1 до @", pages);
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 8 * (page - 1); i < Math.min(8 * page, content.size); i++)
            cons.get(builder.append("**").append(i).append(".** "), content.get(i));

        context.sendEmbed(new EmbedBuilder()
                .setColor(Color.cyan)
                .setTitle(Strings.format(":satellite: Всего игроков на сервере: @", content.size))
                .setDescription(builder.toString())
                .setFooter(Strings.format("Страница @ / @", page, pages)).build());
    }

    // endregion

    public interface BuilderPrefix {
        String get(int page, int pages);
    }
}
