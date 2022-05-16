package pandorum.commands.client;

import arc.math.Mathf;
import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.util.Utils;

import static mindustry.Vars.state;
import static pandorum.PluginVars.*;
import static pandorum.util.Utils.bundled;
import static pandorum.util.Utils.sendToChat;

public class VnwCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (votesVnw.contains(player.uuid())) {
            bundled(player, "commands.already-voted");
            return;
        }

        if (!canVote) {
            Utils.bundled(player, "commands.can-not-vote");
            return;
        }

        votesVnw.add(player.uuid());
        int cur = votesVnw.size;
        int req = Mathf.ceil(voteRatio * Groups.player.size());
        sendToChat("commands.vnw.vote", player.coloredName(), cur, req);

        if (cur < req) return;

        sendToChat("commands.vnw.passed");
        votesVnw.clear();
        state.wavetime = 0f;
    }
}
