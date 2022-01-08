package pandorum.commands.client;

import arc.math.Mathf;
import mindustry.gen.Groups;
import mindustry.gen.Player;

import static mindustry.Vars.state;
import static pandorum.Misc.bundled;
import static pandorum.Misc.sendToChat;
import static pandorum.PluginVars.voteRatio;
import static pandorum.PluginVars.votesVNW;

public class VNWCommand {
    public static void run(final String[] args, final Player player) {
        if (votesVNW.contains(player.uuid())) {
            bundled(player, "commands.already-voted");
            return;
        }

        votesVNW.add(player.uuid());
        int cur = votesVNW.size;
        int req = Mathf.ceil(voteRatio * Groups.player.size());
        sendToChat("commands.vnw.vote", player.coloredName(), cur, req);

        if (cur < req) return;

        sendToChat("commands.vnw.passed");
        votesVNW.clear();
        state.wavetime = 0f;
    }
}
