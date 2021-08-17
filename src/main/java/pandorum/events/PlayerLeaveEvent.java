package pandorum.events;

import arc.struct.ObjectSet;
import arc.util.Log;
import arc.util.Strings;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.Vars;
import pandorum.PandorumPlugin;
import pandorum.comp.Config.PluginType;
import pandorum.comp.DiscordWebhookManager;
import pandorum.effects.Effects;

import static pandorum.Misc.colorizedName;
import static pandorum.Misc.sendToChat;

public class PlayerLeaveEvent {
    public static void call(final EventType.PlayerLeave event) {
        PandorumPlugin.activeHistoryPlayers.remove(event.player.uuid());

        if (Groups.player.size()-1 < 1) Vars.state.serverPaused = true;

        sendToChat("server.player-leave", colorizedName(event.player));
        Log.info(event.player.name + " вышел с сервера, IP: " + event.player.ip() + ", ID: " + event.player.uuid());

        Effects.onLeave(event.player);

        DiscordWebhookManager.client.send(String.format("**%s вышел с сервера!**", Strings.stripColors(event.player.name())));

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
