package pandorum.commands.client;

import mindustry.game.Team;
import mindustry.gen.Player;
import pandorum.Misc;

import static mindustry.Vars.state;
import static pandorum.Misc.bundled;

public class SpectateCommand {
    public static void run(final String[] args, final Player player) {
        if (Misc.adminCheck(player)) return;
        player.clearUnit();
        bundled(player, player.team() == Team.derelict ? "commands.admin.spectate.disabled" : "commands.admin.spectate.enabled");
        player.team(player.team() == Team.derelict ? state.rules.defaultTeam : Team.derelict);
    }
}
