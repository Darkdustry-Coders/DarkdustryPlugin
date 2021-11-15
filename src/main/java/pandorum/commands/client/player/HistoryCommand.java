package pandorum.commands.client.player;

import mindustry.gen.Player;

import static pandorum.Misc.bundled;
import static pandorum.PandorumPlugin.activeHistoryPlayers;

public class HistoryCommand {
    public static void run(final String[] args, final Player player) {
        if (activeHistoryPlayers.contains(player.uuid())) {
            activeHistoryPlayers.remove(player.uuid());
            bundled(player, "commands.history.off");
            return;
        }
        activeHistoryPlayers.add(player.uuid());
        bundled(player, "commands.history.on");
    }
}
