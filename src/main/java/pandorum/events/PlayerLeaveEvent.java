package pandorum.events;

import static pandorum.Misc.*;
import arc.util.Log;
import arc.struct.ObjectSet;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import pandorum.PandorumPlugin;
import pandorum.comp.*;
import pandorum.comp.Config.PluginType;
import pandorum.effects.Effects

import java.io.IOException;
import java.awt.Color;

public class PlayerLeaveEvent {
    public static void call(final EventType.PlayerLeave event) {
        PandorumPlugin.activeHistoryPlayers.remove(event.player.uuid());
        sendToChat("server.player-leave", colorizedName(event.player));
        Log.info(event.player.name + " вышел с сервера, IP: " + event.player.ip() + ", ID: " + event.player.uuid());

        Effects.onLeave(event.player);

        DiscordSender.send(event.player.name, "Вышел с сервера :(", new Color(214, 92, 92));

        PandorumPlugin.rainbow.remove(p -> p.player.uuid().equals(event.player.uuid()));

        if(PandorumPlugin.config.type == PluginType.other) return;
        else if(PandorumPlugin.config.type == PluginType.pvp) {
            ObjectSet<String> uuids = PandorumPlugin.surrendered.get(event.player.team(), ObjectSet::new);
            if(uuids.contains(event.player.uuid())) uuids.remove(event.player.uuid());
        }

        if(PandorumPlugin.votesRTV.contains(event.player.uuid())) {
            PandorumPlugin.votesRTV.remove(event.player.uuid());
            int curRTV = PandorumPlugin.votesRTV.size;
            int reqRTV = (int) Math.ceil(PandorumPlugin.config.voteRatio * Groups.player.size());
            sendToChat("commands.rtv.left", colorizedName(event.player), curRTV, reqRTV);
        }
        if(PandorumPlugin.votesVNW.contains(event.player.uuid())) {
            PandorumPlugin.votesVNW.remove(event.player.uuid());
            int curVNW = PandorumPlugin.votesVNW.size;
            int reqVNW = (int) Math.ceil(PandorumPlugin.config.voteRatio * Groups.player.size());
            sendToChat("commands.vnw.left", colorizedName(event.player), curVNW, reqVNW);
        }
    }
}
