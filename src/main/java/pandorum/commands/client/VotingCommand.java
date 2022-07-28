package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;

import static pandorum.PluginVars.currentVote;
import static pandorum.util.PlayerUtils.bundled;
import static pandorum.util.Utils.voteChoice;

public class VotingCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (currentVote == null) {
            bundled(player, "commands.no-voting");
            return;
        }

        if (currentVote.voted().contains(player.uuid())) {
            bundled(player, "commands.already-voted");
            return;
        }

        int sign = voteChoice(args[0]);
        if (sign == 0) {
            bundled(player, "commands.voting.incorrect-sign");
            return;
        }

        currentVote.vote(player, sign);
    }
}
