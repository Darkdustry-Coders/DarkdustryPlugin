package pandorum.commands.client;

import mindustry.gen.Player;

import static pandorum.PluginVars.currentVote;
import static pandorum.util.Utils.bundled;
import static pandorum.util.Utils.voteChoise;

public class VotingCommand {
    public static void run(final String[] args, final Player player) {
        if (currentVote[0] == null) {
            bundled(player, "commands.no-voting");
            return;
        }

        if (currentVote[0].voted().contains(player.uuid())) {
            bundled(player, "commands.already-voted");
            return;
        }

        int sign = voteChoise(args[0]);
        if (sign == 0) {
            bundled(player, "commands.voting.incorrect-sign");
            return;
        }

        currentVote[0].vote(player, sign);
    }
}
