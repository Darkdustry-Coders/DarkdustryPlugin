package pandorum.commands.client;

import mindustry.gen.Groups;
import mindustry.gen.Player;

import static mindustry.Vars.state;
import static pandorum.Misc.bundled;
import static pandorum.Misc.sendToChat;
import static pandorum.PandorumPlugin.config;
import static pandorum.PandorumPlugin.votesVNW;

public class VNWCommand implements ClientCommand {
    public static void run(final String[] args, final Player player) {
        if (votesVNW.contains(player.uuid())) {
            bundled(player, "commands.already-voted");
            return;
        }

        votesVNW.add(player.uuid());
        int cur = votesVNW.size;
        int req = (int) Math.ceil(config.voteRatio * Groups.player.size());
        sendToChat("commands.vnw.ok", player.coloredName(), cur, req);

        if (cur < req) {
            return;
        }

        sendToChat("commands.vnw.successful");
        votesVNW.clear();
        state.wavetime = 0f;
    }
}
