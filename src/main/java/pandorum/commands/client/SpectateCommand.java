package pandorum.commands.client;

import mindustry.game.Team;
import mindustry.gen.Player;
import pandorum.Misc;
import pandorum.annotations.commands.ClientCommand;
import pandorum.annotations.gamemodes.RequireSimpleGamemode;

import static mindustry.Vars.state;
import static pandorum.Misc.bundled;

public class SpectateCommand {
    private static final Team spectateTeam = Team.derelict;

    @RequireSimpleGamemode
    @ClientCommand(name = "spectate", args = "", description = "Spectator mode", admin = true)
    public static void run(final String[] args, final Player player) {
        if (Misc.adminCheck(player)) return;
        player.clearUnit();
        bundled(player, player.team() == spectateTeam ? "commands.admin.spectate.disabled" : "commands.admin.spectate.enabled");
        player.team(player.team() == spectateTeam ? state.rules.defaultTeam : spectateTeam);
    }
}
