package pandorum.commands.discord;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Strings;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import mindustry.maps.Map;

import static mindustry.Vars.maps;
import static pandorum.discord.Bot.*;

public class MapsListCommand {
    public static void run(final String[] args, final Message message) {
        if (args.length > 0 && !Strings.canParseInt(args[0])) {
            err(message, "Ошибка.", "Страница должна быть числом.");
            return;
        }

        Seq<Map> mapsList = maps.customMaps();
        int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
        int pages = Mathf.ceil(mapsList.size / 20f);

        if (--page >= pages || page < 0) {
            err(message, "Указана неверная страница списка карт.", "Страница должна быть числом от 1 до @", pages);
            return;
        }

        StringBuilder maps = new StringBuilder();
        for (int i = 20 * page; i < Math.min(20 * (page + 1), mapsList.size); i++) {
            maps.append("**").append(i + 1).append(".** ").append(Strings.stripColors(mapsList.get(i).name())).append("\n");
        }

        sendEmbed(message, EmbedCreateSpec.builder()
                .color(normalColor)
                .title(Strings.format("Список карт сервера (страница @ из @)", page + 1, pages))
                .addField(Strings.format("Всего карт: @", mapsList.size), maps.toString(), false)
                .build()
        );
    }
}
