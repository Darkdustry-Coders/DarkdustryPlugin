package pandorum.commands.client;

import arc.util.Timekeeper;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration;
import pandorum.vote.VoteKickSession;

import java.util.concurrent.TimeUnit;

import static pandorum.Misc.bundled;
import static pandorum.Misc.findPlayer;
import static pandorum.PluginVars.*;

public class VoteKickCommand {
    public static void run(final String[] args, final Player player) {
        if (!Administration.Config.enableVotekick.bool()) {
            bundled(player, "commands.votekick.disabled");
            return;
        }

        if (Groups.player.size() < 3) {
            bundled(player, "commands.votekick.not-enough-players");
            return;
        }

        if (currentVotekick[0] != null) {
            bundled(player, "commands.vote-already-started");
            return;
        }

        Timekeeper vtime = votekickCooldowns.get(player.uuid(), () -> new Timekeeper(votekickCooldownTime));
        if (!vtime.get() && !player.admin) {
            bundled(player, "commands.votekick.cooldown", TimeUnit.SECONDS.toMinutes(votekickCooldownTime));
            return;
        }

        Player target = findPlayer(args[0]);
        if (target == null) {
            bundled(player, "commands.player-not-found");
            return;
        }

        if (target == player) {
            bundled(player, "commands.votekick.player-is-you");
            return;
        }

        if (target.admin) {
            bundled(player, "commands.votekick.player-is-admin");
            return;
        }

        if (target.team() != player.team()) {
            bundled(player, "commands.votekick.player-is-enemy");
            return;
        }

        VoteKickSession session = new VoteKickSession(currentVotekick, target);
        currentVotekick[0] = session;
        session.vote(player, 1);
        vtime.reset();
    }
}
