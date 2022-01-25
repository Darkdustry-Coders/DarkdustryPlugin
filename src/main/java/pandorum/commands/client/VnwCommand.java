package pandorum.commands.client;

import arc.math.Mathf;
import mindustry.gen.Groups;
import mindustry.gen.Player;

import static mindustry.Vars.state;
import static pandorum.utils.Utils.bundled;
import static pandorum.utils.Utils.sendToChat;
import static pandorum.PluginVars.voteRatio;
import static pandorum.PluginVars.votesVnw;

public class VnwCommand {
    public static void run(final String[] args, final Player player) {
        if (votesVnw.contains(player.uuid())) {
            bundled(player, "commands.already-voted");
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
