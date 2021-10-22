package pandorum.commands.client;

import mindustry.gen.Player;

import static pandorum.Misc.bundled;
import static pandorum.PandorumPlugin.currentlyKicking;

public class VoteCommand implements ClientCommand {
    public static void run(final String[] args, final Player player) {
        if (currentlyKicking[0] == null) {
            bundled(player, "commands.no-voting");
            return;
        }

        if (currentlyKicking[0].voted().contains(player.uuid())) {
            bundled(player, "commands.already-voted");
            return;
        }

        if (currentlyKicking[0].target() == player) {
            bundled(player, "commands.vote.cannot-vote-for-yourself");
            return;
        }

        if (currentlyKicking[0].target().team() != player.team()) {
            bundled(player, "commands.vote.cannot-vote-another-team");
            return;
        }

        int sign = switch(args[0].toLowerCase()) {
            case "y", "yes", "да" ->  1;
            case "n", "no", "нет" -> -1;
            default -> 0;
        };

        if (sign == 0) {
            bundled(player, "commands.vote.incorrect-sign");
            return;
        }

        currentlyKicking[0].vote(player, sign);
    }
}
