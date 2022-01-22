package pandorum.commands.discord;

import discord4j.core.object.entity.Message;
import mindustry.maps.Map;

import static mindustry.Vars.maps;
import static pandorum.Misc.findMap;
import static pandorum.discord.Bot.err;
import static pandorum.discord.Bot.text;

public class RemoveMapCommand {
    public static void run(final String[] args, final Message message) {
        maps.reload();
        Map map = findMap(args[0]);
        if (map == null) {
            err(message.getChannel().block(), "Карта не найдена.", "Проверьте правильность ввода.");
            return;
        }

        try {
            maps.removeMap(map);
            maps.reload();
            text(message.getChannel().block(), "*Карта удалена с сервера.*");
        } catch (Exception e) {
            err(message.getChannel().block(), "Ошибка.", "Удалить карту с сервера не удалось.");
        }
    }
}
