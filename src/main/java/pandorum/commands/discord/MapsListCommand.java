package pandorum.commands.discord;

import arc.util.CommandHandler.CommandRunner;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.maps.Map;
import net.dv8tion.jda.api.EmbedBuilder;
import pandorum.discord.Context;

import java.awt.*;

import static mindustry.Vars.maps;

public class MapsListCommand implements CommandRunner<Context> {
    public void accept(String[] args, Context context) {
        if (args.length > 0 && !Strings.canParseInt(args[0])) {
            context.err(":interrobang: Страница должна быть числом.");
            return;
        }

        Seq<Map> mapsList = maps.customMaps();
        if (mapsList.isEmpty()) {
            context.info(":map: На сервере нет карт.");
            return;
        }

        int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
        int pages = Mathf.ceil(mapsList.size / 16f);

        if (--page >= pages || page < 0) {
            context.err(":interrobang: Неверная страница.", "Страница должна быть числом от 1 до @", pages);
            return;
        }

        StringBuilder maps = new StringBuilder();
        for (int i = 16 * page; i < Math.min(16 * (page + 1), mapsList.size); i++) {
            Map map = mapsList.get(i);
            maps.append("**").append(i).append(".** ").append(Strings.stripColors(map.name())).append("\n");
        }

        context.sendEmbed(new EmbedBuilder()
                .setColor(Color.cyan)
                .setTitle(Strings.format(":map: Всего карт на сервере: @", mapsList.size))
                .setDescription(maps.toString())
                .setFooter(Strings.format("Страница @ / @", page + 1, pages))
                .build());
    }
}
