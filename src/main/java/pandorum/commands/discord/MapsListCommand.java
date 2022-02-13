package pandorum.commands.discord;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.maps.Map;
import net.dv8tion.jda.api.EmbedBuilder;
import pandorum.discord.Context;

import java.awt.*;

import static mindustry.Vars.maps;

public class MapsListCommand {
    public static void run(final String[] args, final Context context) {
        if (args.length > 0 && !Strings.canParseInt(args[0])) {
            context.err(":interrobang: Page must be a number.");
            return;
        }

        Seq<Map> mapsList = maps.customMaps();
        if (mapsList.isEmpty()) {
            context.info(":map: No maps on the server.");
            return;
        }

        int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
        int pages = Mathf.ceil(mapsList.size / 16f);

        if (--page >= pages || page < 0) {
            context.err(":interrobang: Invalid page.", "Page should be a number from 1 to @", pages);
            return;
        }

        StringBuilder maps = new StringBuilder();
        for (int i = 16 * page; i < Math.min(16 * (page + 1), mapsList.size); i++) {
            Map map = mapsList.get(i);
            maps.append("**").append(i + 1).append(".** ").append(Strings.stripColors(map.name())).append("\n");
        }

        context.sendEmbed(new EmbedBuilder()
                .setColor(Color.cyan)
                .setTitle(Strings.format(":map: Total maps in playlist: @", mapsList.size))
                .setDescription(maps.toString())
                .setFooter(Strings.format("Page @ / @", page + 1, pages))
                .build());
    }
}
