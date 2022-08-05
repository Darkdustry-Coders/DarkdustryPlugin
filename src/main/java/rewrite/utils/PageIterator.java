package rewrite.utils;

import arc.func.Cons;
import arc.func.Cons3;
import arc.math.Mathf;
import arc.struct.Seq;
import mindustry.gen.Groups;
import mindustry.gen.Iconc;
import mindustry.gen.Player;
import mindustry.io.SaveIO;

import java.util.Locale;

import static arc.util.Strings.*;
import static mindustry.Vars.*;
import static rewrite.PluginVars.*;
import static rewrite.components.Bundle.*;

/** Страшно, но очень полезно. */
public class PageIterator { // TODO: сделаешь ещё итераторы для карт и игроков в дискорде

    public static void commands(String[] args, Player player) {
        Locale locale = Find.locale(player.name);
        client(args, player, clientCommands.getCommandList(), "help",
                (builder, i, command) -> {
                    builder.append("\n[orange] ").append(clientCommands.getPrefix()).append(command.text).append("[white] ")
                            .append(get("commands." + command.text + ".params", command.paramText, locale)).append("[lightgray] - ")
                            .append(get("commands." + command.text + ".description", command.description, locale));
                }, null);
    }

    public static void players(String[] args, Player player) {
        client(args, player, Groups.player.copy(new Seq<>()), "players",
                (builder, i, p) -> {
                    builder.append("\n[#9c88ee]* [white]");
                    if (p.admin) builder.append(Iconc.admin).append(" ");
                    builder.append(p.name).append(" [lightgray]([accent]ID: ").append(p.id).append("[lightgray])").append(" [lightgray]([accent]Locale: ").append(p.locale).append("[lightgray])");
                }, null);
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

    public static <T> void client(String[] args, Player player, Seq<T> content, String command, Cons3<StringBuilder, Integer, T> cons, Cons<StringBuilder> result) {
        iterate(content, 8, args, (page, pages) -> format("commands." + command + ".page", Find.locale(player.locale), page, pages),
                pages -> bundled(player, "commands.page-not-int"),
                pages -> {},
                pages -> bundled(player, "commands.under-page", pages),
                cons, builder -> {
                    if (result != null) result.get(builder);
                    player.sendMessage(builder.toString());
                });
    }

    public static <T> void iterate(
            Seq<T> content, int size, String[] args, BuilderPrefix start,
            Cons<Integer> notint, Cons<Integer> empty, Cons<Integer> outrange,
            Cons3<StringBuilder, Integer, T> cons, Cons<StringBuilder> result) {

        if (args.length > 0 && !canParseInt(args[0])) {
            notint.get(0);
            return;
        }

        if (content.isEmpty()) {
            empty.get(0);
            return;
        }

        int page = args.length > 0 ? parseInt(args[0]) : 1, pages = Mathf.ceil(content.size / (float) size);

        if (page - 1 >= pages || page <= 0) {
            outrange.get(pages);
            return;
        }

        StringBuilder builder = new StringBuilder(start.get(page, pages));
        for (int i = size * (page - 1); i < Math.min(size * page, content.size); i++)
            cons.get(builder, i, content.get(i));

        result.get(builder);
    }

    public interface BuilderPrefix {
        String get(int page, int pages);
    }
}
