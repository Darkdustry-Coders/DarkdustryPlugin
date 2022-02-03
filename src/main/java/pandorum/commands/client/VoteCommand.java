package pandorum.commands.client;

import mindustry.gen.Player;

import static pandorum.util.Utils.bundled;
import static pandorum.PluginVars.currentVotekick;

public class VoteCommand {
    public static void run(final String[] args, final Player player) {
        if (currentVotekick == null) {
            bundled(player, "commands.no-voting");
            return;
        }

        if (currentVotekick.voted().contains(player.uuid())) {
            bundled(player, "commands.already-voted");
            return;
        }

        if (currentVotekick.target() == player) {
            bundled(player, "commands.vote.player-is-you");
            return;
        }

        if (currentVotekick.target().team() != player.team()) {
            bundled(player, "commands.vote.player-is-enemy");
            return;
        }

        int sign = switch (args[0].toLowerCase()) {
            case "y", "yes", "+", "д", "да" -> 1;
            case "n", "no", "-", "н", "нет" -> -1;
            default -> 0;
        };

        if (sign == 0) {
            bundled(player, "commands.vote.incorrect-sign");
            return;
        }

        currentVotekick.vote(player, sign);
    }
}
