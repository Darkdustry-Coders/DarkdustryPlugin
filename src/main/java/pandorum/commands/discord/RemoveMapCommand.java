package pandorum.commands.discord;

import mindustry.maps.Map;
import net.dv8tion.jda.api.entities.Message;

import static mindustry.Vars.maps;
import static pandorum.util.Search.findMap;
import static pandorum.discord.Bot.err;
import static pandorum.discord.Bot.text;

public class RemoveMapCommand {
    public static void run(final String[] args, final Message message) {
        Map map = findMap(args[0]);
        if (map == null) {
            err(message.getChannel(), ":interrobang: Карта не найдена.", "Проверьте правильность ввода.");
            return;
        }

        try {
            maps.removeMap(map);
            maps.reload();
            text(message.getChannel(), ":white_check_mark: Карта удалена с сервера.");
        } catch (Exception e) {
            err(message.getChannel(), ":x: Ошибка.", "Удалить карту с сервера не удалось.");
        }
    }
}
