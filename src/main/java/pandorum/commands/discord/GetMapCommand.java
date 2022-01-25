package pandorum.commands.discord;

import mindustry.maps.Map;
import net.dv8tion.jda.api.entities.Message;

import static pandorum.utils.Search.findMap;
import static pandorum.discord.Bot.err;

public class GetMapCommand {
    public static void run(final String[] args, final Message message) {
        Map map = findMap(args[0]);
        if (map == null) {
            err(message.getChannel(), ":interrobang: Карта не найдена.", "Проверьте правильность ввода.");
            return;
        }

        try {
            message.getChannel().sendFile(map.file.file()).queue();
        } catch (Exception e) {
            err(message.getChannel(), ":x: Ошибка.", "Получить карту с сервера не удалось.");
        }
    }
}
