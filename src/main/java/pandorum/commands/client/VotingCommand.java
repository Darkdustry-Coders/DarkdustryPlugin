package pandorum.commands.client;

import mindustry.gen.Player;

import static pandorum.Misc.bundled;
import static pandorum.PandorumPlugin.current;

public class VotingCommand implements ClientCommand {
    public static void run(final String[] args, final Player player) {
        if (current[0] == null) {
            bundled(player, "commands.no-voting");
            return;
        }

        if (current[0].voted().contains(player.uuid())) {
            bundled(player, "commands.already-voted");
            return;
        }

        int sign = switch(args[0].toLowerCase()) {
            case "y", "yes", "да" ->  1;
            case "n", "no", "нет" -> -1;
            default -> 0;
        };

        if (sign == 0) {
            bundled(player, "commands.voting.incorrect-sign");
            return;
        }

        current[0].vote(player, sign);
    }
}
