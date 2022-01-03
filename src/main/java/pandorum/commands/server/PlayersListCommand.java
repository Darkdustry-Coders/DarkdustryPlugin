package pandorum.commands.server;

import arc.util.Log;
import mindustry.gen.Groups;

public class PlayersListCommand {
    public static void run(final String[] args) {
        if (Groups.player.isEmpty()) {
            Log.info("На сервере нет игроков.");
        } else {
            Log.info("Игроки: (@)", Groups.player.size());
            Groups.player.each(player -> Log.info(" &lm @ /  UUID: @ / IP: @ / Админ: @", player.name, player.uuid(), player.ip(), player.admin));
        }
    }
}
