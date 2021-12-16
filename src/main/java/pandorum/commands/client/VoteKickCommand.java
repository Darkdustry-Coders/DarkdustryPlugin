package pandorum.commands.client;

import arc.util.Timekeeper;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration;
import pandorum.vote.VoteKickSession;

import static pandorum.Misc.bundled;
import static pandorum.Misc.findByName;
import static pandorum.PandorumPlugin.currentlyKicking;
import static pandorum.PandorumPlugin.votekickCooldowns;

public class VoteKickCommand {

    private static final float cooldownTime = 300f;

    public static void run(final String[] args, final Player player) {
        if (!Administration.Config.enableVotekick.bool()) {
            bundled(player, "commands.votekick.disabled");
            return;
        }

        if (Groups.player.size() < 3) {
            bundled(player, "commands.votekick.not-enough-players");
            return;
        }

        if (currentlyKicking[0] != null) {
            bundled(player, "commands.vote-already-started");
            return;
        }

        Timekeeper vtime = votekickCooldowns.get(player.uuid(), () -> new Timekeeper(cooldownTime));
        if (!vtime.get()) {
            bundled(player, "commands.votekick.cooldown", (int) (cooldownTime / 60f));
            return;
        }

        Player target = findByName(args[0]);
        if (target == null) {
            bundled(player, "commands.player-not-found");
            return;
        }

        if (target == player) {
            bundled(player, "commands.votekick.player-is-you");
            return;
        }

        if (target.admin()) {
            bundled(player, "commands.votekick.player-is-admin");
            return;
        }

        if (target.team() != player.team()) {
            bundled(player, "commands.votekick.player-is-enemy");
            return;
        }

        VoteKickSession session = new VoteKickSession(currentlyKicking, target);
        currentlyKicking[0] = session;
        vtime.reset();
        session.vote(player, 1);
    }
}
