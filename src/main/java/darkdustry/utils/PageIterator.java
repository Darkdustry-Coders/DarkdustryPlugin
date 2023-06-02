package darkdustry.utils;

import arc.func.*;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Strings;
import darkdustry.discord.MessageContext;
import mindustry.gen.Groups;

import static arc.util.Strings.*;
import static darkdustry.PluginVars.*;
import static darkdustry.utils.Utils.*;

// Страшно, но очень полезно.
// (C) xzxADIxzx, 2023 год
public class PageIterator {

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

                getAvailableMaps(),

                (map, index) -> format("**@.** @", index, stripColors(map.name())),
                (map) -> format("Author: @\nSize: @x@", stripColors(map.author()), map.width, map.height)
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