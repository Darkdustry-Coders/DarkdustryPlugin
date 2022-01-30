package pandorum.commands.discord;

import mindustry.maps.Map;
import pandorum.discord.Context;

import static mindustry.Vars.maps;
import static pandorum.util.Search.findMap;

public class RemoveMapCommand {
    public static void run(final String[] args, final Context context) {
        Map map = findMap(args[0]);
        if (map == null) {
            context.err(":mag: Карта не найдена.", "Проверьте правильность ввода.");
            return;
        }

        try {
            maps.removeMap(map);
            maps.reload();
            context.success(":white_check_mark: Успешно.", "Карта удалена с сервера.");
        } catch (Exception e) {
            context.err(":x: Ошибка.", "Удалить карту с сервера не удалось.");
        }
    }
}
