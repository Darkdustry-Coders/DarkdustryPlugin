package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;

import static pandorum.PluginVars.currentVoteKick;
import static pandorum.util.Utils.bundled;
import static pandorum.util.Utils.voteChoice;

public class VoteCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (currentVoteKick[0] == null) {
            bundled(player, "commands.no-voting");
            return;
        }

        if (currentVoteKick[0].voted().contains(player.uuid())) {
            bundled(player, "commands.already-voted");
            return;
        }

        if (currentVoteKick[0].target() == player) {
            bundled(player, "commands.vote.player-is-you");
            return;
        }

        if (currentVoteKick[0].target().team() != player.team()) {
            bundled(player, "commands.vote.player-is-enemy");
            return;
        }

        int sign = voteChoice(args[0]);
        if (sign == 0) {
            bundled(player, "commands.vote.incorrect-sign");
            return;
        }

        currentVoteKick[0].vote(player, sign);
    }
}
