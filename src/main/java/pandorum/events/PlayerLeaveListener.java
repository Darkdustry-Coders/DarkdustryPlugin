package pandorum.events;

import arc.struct.Seq;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import org.bson.Document;
import pandorum.PandorumPlugin;
import pandorum.vote.VoteKickSession;
import pandorum.comp.Config.Gamemode;
import pandorum.effects.Effects;

import static pandorum.Misc.sendToChat;
import static pandorum.Misc.colorizedTeam;

public class PlayerLeaveListener {
    public static void call(final EventType.PlayerLeave event) {

        sendToChat("events.player-leave", event.player.coloredName());
        Log.info("@ вышел с сервера, IP: @, ID: @", event.player.name, event.player.ip(), event.player.uuid());

        Effects.onLeave(event.player);

        Document playerInfo = PandorumPlugin.createInfo(event.player);
        long time = Time.timeSinceMillis(event.player.con.connectTime) + playerInfo.getLong("playtime");
        playerInfo.replace("playtime", time);
        PandorumPlugin.savePlayerStats(event.player.uuid());

        PandorumPlugin.rainbow.remove(p -> p.player.uuid().equals(event.player.uuid()));
        PandorumPlugin.activeHistoryPlayers.remove(event.player.uuid());

        if (PandorumPlugin.currentlyKicking[0] != null && PandorumPlugin.currentlyKicking[0].target().uuid().equals(event.player.uuid())) {
            PandorumPlugin.currentlyKicking[0].stop();
            event.player.getInfo().lastKicked = Time.millis() + VoteKickSession.kickDuration * 1000L;
            sendToChat("commands.votekick.left", event.player.coloredName(), VoteKickSession.kickDuration / 60f);
        }

        if (PandorumPlugin.config.mode == Gamemode.pvp || PandorumPlugin.config.mode == Gamemode.siege) {
            Seq<String> teamVotes = PandorumPlugin.surrendered.get(event.player.team(), Seq::new);
            if (teamVotes.contains(event.player.uuid())) {
                teamVotes.remove(event.player.uuid());
                sendToChat("commands.surrender.left", colorizedTeam(event.player.team()), event.player.coloredName(), teamVotes.size, (int)Math.ceil(PandorumPlugin.config.voteRatio * Groups.player.count(p -> p.team() == event.player.team())));
            }
        }

        if (PandorumPlugin.votesRTV.contains(event.player.uuid())) {
            PandorumPlugin.votesRTV.remove(event.player.uuid());
            sendToChat("commands.rtv.left", event.player.coloredName(), PandorumPlugin.votesRTV.size, (int)Math.ceil(PandorumPlugin.config.voteRatio * Groups.player.size()));
        }

        if (PandorumPlugin.votesVNW.contains(event.player.uuid())) {
            PandorumPlugin.votesVNW.remove(event.player.uuid());
            sendToChat("commands.vnw.left", event.player.coloredName(), PandorumPlugin.votesVNW.size, (int)Math.ceil(PandorumPlugin.config.voteRatio * Groups.player.size()));
        }
    }
}
