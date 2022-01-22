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
        } else {
            activeSpectatingPlayers.put(target.uuid(), target.team());
            target.clearUnit();
            target.team(spectateTeam);
        }

        bundled(target, activeSpectatingPlayers.containsKey(target.uuid()) ? "commands.admin.spectate.success.enabled" : "commands.admin.spectate.success.disabled");
        if (target != player)
            bundled(player, activeSpectatingPlayers.containsKey(target.uuid()) ? "commands.admin.spectate.changed.enabled" : "commands.admin.spectate.changed.disabled", target.coloredName());
    }
}
