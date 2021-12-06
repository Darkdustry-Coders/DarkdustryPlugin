package pandorum.commands.client;

import arc.Events;
import mindustry.game.EventType.GameOverEvent;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.annotations.commands.ClientCommand;
import pandorum.annotations.gamemodes.RequireSimpleGamemode;

import static mindustry.Vars.state;
import static pandorum.Misc.bundled;
import static pandorum.Misc.sendToChat;
import static pandorum.PandorumPlugin.config;
import static pandorum.PandorumPlugin.votesRTV;

public class RTVCommand {
    @RequireSimpleGamemode
    @ClientCommand(name = "rtv", args = "", description = "Vote to skip the map.", admin = false)
    public static void run(final String[] args, final Player player) {
        if (votesRTV.contains(player.uuid())) {
            bundled(player, "commands.already-voted");
            return;
        }

        votesRTV.add(player.uuid());
        int cur = votesRTV.size;
        int req = (int) Math.ceil(config.voteRatio * Groups.player.size());
        sendToChat("commands.rtv.ok", player.coloredName(), cur, req);

        if (cur < req) return;

        sendToChat("commands.rtv.successful");
        votesRTV.clear();
        Events.fire(new GameOverEvent(state.rules.waveTeam));
    }
}
