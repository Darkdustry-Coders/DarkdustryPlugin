package pandorum.commands.client;

import mindustry.game.Team;
import mindustry.gen.Player;

import static mindustry.Vars.state;
import static pandorum.Misc.bundled;
import static pandorum.PandorumPlugin.spectating;

public class SpectateCommand {

    private static final Team spectateTeam = Team.derelict;

    public static void run(final String[] args, final Player player) {
        if (spectating.containsKey(player.uuid())) {
            player.team(spectating.get(player.uuid()));
            spectating.remove(player.uuid());
            bundled(player, "commands.admin.spectate.disabled");
            return;
        }

        spectating.put(player.uuid(), player.team());
        player.clearUnit();
        player.team(spectateTeam);
        bundled(player, "commands.admin.spectate.enabled");
    }
}
