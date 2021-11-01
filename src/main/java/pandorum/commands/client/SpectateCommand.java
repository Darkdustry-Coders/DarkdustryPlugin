package pandorum.commands.client;

import mindustry.game.Team;
import mindustry.gen.Player;
import pandorum.Misc;

import static mindustry.Vars.state;
import static pandorum.Misc.bundled;

public class SpectateCommand implements ClientCommand {

    public static Team spectateTeam = Team.derelict;

    public static void run(final String[] args, final Player player) {
        if (Misc.adminCheck(player)) return;
        player.clearUnit();
        bundled(player, player.team() == spectateTeam ? "commands.admin.spectate.disabled" : "commands.admin.spectate.enabled");
        player.team(player.team() == spectateTeam ? state.rules.defaultTeam : spectateTeam);
    }
}
