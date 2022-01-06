package pandorum.commands.client;

import mindustry.gen.Player;

import static pandorum.Misc.bundled;
import static pandorum.PluginVars.activeSpectatingPlayers;
import static pandorum.PluginVars.spectateTeam;

public class SpectateCommand {
    public static void run(final String[] args, final Player player) {
        if (activeSpectatingPlayers.containsKey(player.uuid())) {
            player.team(activeSpectatingPlayers.remove(player.uuid()));
            bundled(player, "commands.admin.spectate.disabled");
            return;
        }

        activeSpectatingPlayers.put(player.uuid(), player.team());
        player.clearUnit();
        player.team(spectateTeam);
        bundled(player, "commands.admin.spectate.enabled");
    }
}
