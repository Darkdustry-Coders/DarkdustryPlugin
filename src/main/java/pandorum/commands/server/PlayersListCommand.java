package pandorum.commands.server;

import arc.struct.Seq;
import arc.util.Log;
import mindustry.gen.Groups;
import mindustry.gen.Player;

public class PlayersListCommand {
    public static void run(final String[] args) {
        Seq<Player> playersList = Groups.player.copy(new Seq<>());
        if (playersList.isEmpty()) {
            Log.info("На сервере нет игроков.");
        } else {
            Log.info("Игроки: (@)", playersList.size);
            playersList.each(player -> Log.info("  &lm'@' /  UUID: '@' / IP: '@' / Админ: '@'", player.name, player.uuid(), player.ip(), player.admin));
        }
    }
}
