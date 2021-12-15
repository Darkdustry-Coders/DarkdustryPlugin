package pandorum.commands.client;

import arc.util.Timekeeper;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration;
import pandorum.annotations.commands.ClientCommand;
import pandorum.annotations.commands.OverrideCommand;
import pandorum.vote.VoteKickSession;

import static pandorum.Misc.bundled;
import static pandorum.Misc.findByName;
import static pandorum.PandorumPlugin.currentlyKicking;
import static pandorum.PandorumPlugin.votekickCooldowns;

public class VoteKickCommand {

    private static final float cooldownTime = 300f;
    @OverrideCommand
    @ClientCommand(name = "votekick", args = "<player...>", description = "Start a voting to kick a player.")
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

        Player found = findByName(args[0]);
        if (found == null) {
            bundled(player, "commands.player-not-found");
            return;
        }

        if (found.admin()) {
            bundled(player, "commands.votekick.cannot-kick-admin");
            return;
        }

        if (found.team() != player.team()) {
            bundled(player, "commands.votekick.cannot-kick-another-team");
            return;
        }

        if (found == player) {
            bundled(player, "commands.votekick.cannot-vote-for-yourself");
            return;
        }

        VoteKickSession session = new VoteKickSession(currentlyKicking, found);
        currentlyKicking[0] = session;
        vtime.reset();
        session.vote(player, 1);
    }
}
