package pandorum.commands.client;

import mindustry.gen.Player;
import pandorum.annotations.commands.ClientCommand;
import pandorum.annotations.commands.gamemodes.RequireSimpleGamemode;

import static pandorum.Misc.bundled;
import static pandorum.PandorumPlugin.activeHistoryPlayers;

public class HistoryCommand {
    @RequireSimpleGamemode
    @ClientCommand(name = "history", args = "", description = "Enable tile inspector.")
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
