package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import arc.util.Timekeeper;
import mindustry.gen.Player;
import mindustry.net.Administration.Config;
import pandorum.util.Utils;
import pandorum.vote.VoteKickSession;

import static pandorum.PluginVars.*;
import static pandorum.util.Search.findPlayer;

public class VoteKickCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (!Config.enableVotekick.bool()) {
            Utils.bundled(player, "commands.votekick.disabled");
            return;
        }

        if (currentVotekick[0] != null) {
            Utils.bundled(player, "commands.vote-already-started");
            return;
        }

        Timekeeper vtime = votekickCooldowns.get(player.uuid(), () -> new Timekeeper(votekickCooldownTime));
        if (!vtime.get() && !player.admin) {
            Utils.bundled(player, "commands.votekick.cooldown", Utils.secondsToMinutes(votekickCooldownTime));
            return;
        }

        Player target = findPlayer(args[0]);
        if (target == null) {
            Utils.bundled(player, "commands.player-not-found", args[0]);
            return;
        }

        if (target == player) {
            Utils.bundled(player, "commands.votekick.player-is-you");
            return;
        }

        if (target.admin) {
            Utils.bundled(player, "commands.votekick.player-is-admin");
            return;
        }

        if (target.team() != player.team()) {
            Utils.bundled(player, "commands.votekick.player-is-enemy");
            return;
        }

        VoteKickSession session = new VoteKickSession(currentVotekick, player, target);
        currentVotekick[0] = session;
        session.vote(player, 1);
        Utils.bundled(target, "commands.votekick.do-not-leave", Utils.millisecondsToMinutes(kickDuration));
        vtime.reset();
    }
}
