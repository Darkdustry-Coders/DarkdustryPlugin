package pandorum.commands.client;

import static mindustry.Vars.state;
import static pandorum.Misc.bundled;
import static pandorum.Misc.sendToChat;
import static pandorum.PandorumPlugin.config;
import static pandorum.PandorumPlugin.votesRTV;

import arc.Events;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.Misc;

public class RTVCommand implements ClientCommand {
    public static void run(final String[] args, final Player player) {
        if (votesRTV.contains(player.uuid())) {
            bundled(player, "commands.already-voted");
            return;
        }

        votesRTV.add(player.uuid());
        int cur = votesRTV.size;
        int req = (int)Math.ceil(config.voteRatio * Groups.player.size());
        sendToChat("commands.rtv.ok", Misc.colorizedName(player), cur, req);

        if (cur < req) {
            return;
        }

        sendToChat("commands.rtv.successful");
        votesRTV.clear();
        Events.fire(new EventType.GameOverEvent(state.rules.waveTeam));
    }
}
