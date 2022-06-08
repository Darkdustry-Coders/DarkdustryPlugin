package pandorum.commands.discord;

import arc.util.CommandHandler.CommandRunner;
import mindustry.maps.Map;
import pandorum.discord.Context;

import static mindustry.Vars.maps;
import static pandorum.util.PlayerUtils.isAdmin;
import static pandorum.util.Search.findMap;

public class RemoveMapCommand implements CommandRunner<Context> {
    public void accept(String[] args, Context context) {
        if (!isAdmin(context.member)) {
            context.err(":no_entry_sign: Эта команда только для администрации.", "У тебя нет прав на ее использование.");
            return;
        }

        Map map = findMap(args[0]);
        if (map == null) {
            context.err(":mag: Карта не найдена.", "Проверь, правильно ли введено название.");
            return;
        }

        maps.removeMap(map);
        maps.reload();
        context.success(":dagger: Карта удалена с сервера.");
    }
}
