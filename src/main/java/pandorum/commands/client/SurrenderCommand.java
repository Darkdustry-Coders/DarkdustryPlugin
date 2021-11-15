package pandorum.commands.client;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.content.Blocks;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.world.Tile;
import pandorum.Misc;

import static mindustry.Vars.world;
import static pandorum.Misc.bundled;
import static pandorum.Misc.sendToChat;
import static pandorum.PandorumPlugin.config;
import static pandorum.PandorumPlugin.surrendered;

public class SurrenderCommand {
    public static void run(final String[] args, final Player player) {
        Seq<String> teamVotes = surrendered.get(player.team(), Seq::new);
        if (teamVotes.contains(player.uuid())) {
            bundled(player, "commands.already-voted");
            return;
        }

        teamVotes.add(player.uuid());
        int cur = teamVotes.size;
        int req = (int) Math.ceil(config.voteRatio * Groups.player.count(p -> p.team() == player.team()));
        sendToChat("commands.surrender.ok", Misc.colorizedTeam(player.team()), player.coloredName(), cur, req);

        if (cur < req) return;

        surrendered.remove(player.team());
        sendToChat("commands.surrender.successful", Misc.colorizedTeam(player.team()));
        for (Tile tile : world.tiles) {
            if (tile.build != null && tile.block() != Blocks.air && tile.team() == player.team()) {
                Time.run(Mathf.random(360f), tile::removeNet);
            }
        }
        Groups.unit.each(u -> u.team == player.team(), u -> Time.run(Mathf.random(360f), u::kill));
    }
}
