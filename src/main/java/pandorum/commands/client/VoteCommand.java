package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;

import static pandorum.PluginVars.currentVotekick;
import static pandorum.util.Utils.bundled;
import static pandorum.util.Utils.voteChoise;

public class VoteCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (currentVotekick[0] == null) {
            bundled(player, "commands.no-voting");
            return;
        }

        if (currentVotekick[0].voted().contains(player.uuid())) {
            bundled(player, "commands.already-voted");
            return;
        }

        if (currentVotekick[0].target() == player) {
            bundled(player, "commands.vote.player-is-you");
            return;
        }

        if (currentVotekick[0].target().team() != player.team()) {
            bundled(player, "commands.vote.player-is-enemy");
            return;
        }

        int sign = voteChoise(args[0]);
        if (sign == 0) {
            bundled(player, "commands.vote.incorrect-sign");
            return;
        }

        currentVotekick[0].vote(player, sign);
    }
}
