package pandorum.commands.client;

import mindustry.gen.Player;

import static pandorum.utils.Utils.bundled;
import static pandorum.PluginVars.activeHistoryPlayers;

public class HistoryCommand {
    public static void run(final String[] args, final Player player) {
        if (activeHistoryPlayers.remove(player.uuid())) {
            bundled(player, "commands.history.off");
            return;
        }

        activeHistoryPlayers.add(player.uuid());
        bundled(player, "commands.history.on");
    }
}
