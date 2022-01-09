package pandorum.commands.client;

import mindustry.gen.Player;

import static pandorum.Misc.bundled;
import static pandorum.Misc.findPlayer;
import static pandorum.PluginVars.activeSpectatingPlayers;
import static pandorum.PluginVars.spectateTeam;

public class SpectateCommand {
    public static void run(final String[] args, final Player player) {
        Player target = args.length > 0 ? findPlayer(args[0]) : player;
        if (target == null) {
            bundled(player, "commands.player-not-found", args[0]);
            return;
        }

        if (activeSpectatingPlayers.containsKey(target.uuid())) {
            target.team(activeSpectatingPlayers.remove(target.uuid()));
            bundled(target, "commands.admin.spectate.disabled");
            return;
        }

        activeSpectatingPlayers.put(target.uuid(), target.team());
        target.clearUnit();
        target.team(spectateTeam);
        bundled(target, "commands.admin.spectate.enabled");
    }
}
