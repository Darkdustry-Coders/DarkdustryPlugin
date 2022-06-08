package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;

import static pandorum.PluginVars.activeSpectatingPlayers;
import static pandorum.PluginVars.spectateTeam;
import static pandorum.util.PlayerUtils.bundled;
import static pandorum.util.PlayerUtils.isAdmin;
import static pandorum.util.Search.findPlayer;

public class SpectateCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (!isAdmin(player)) {
            bundled(player, "commands.permission-denied");
            return;
        }

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
