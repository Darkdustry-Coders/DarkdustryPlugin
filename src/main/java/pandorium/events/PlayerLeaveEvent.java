package pandorium.events;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;

import arc.struct.Seq;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.Vars;
import pandorium.PandorumPlugin;
import pandorium.vote.VoteKickSession;
import pandorium.comp.Config.PluginType;
import pandorium.comp.DiscordWebhookManager;
import pandorium.effects.Effects;

import static pandorium.Misc.colorizedName;
import static pandorium.Misc.sendToChat;
import static pandorium.Misc.colorizedTeam;

public class PlayerLeaveEvent {
    public static void call(final EventType.PlayerLeave event) {
        PandorumPlugin.activeHistoryPlayers.remove(event.player.uuid());

        if (Groups.player.size()-1 < 1) Vars.state.serverPaused = true;

        sendToChat("events.player-leave", colorizedName(event.player));
        Log.info(event.player.name + " вышел с сервера, IP: " + event.player.ip() + ", ID: " + event.player.uuid());

        Effects.onLeave(event.player);

        WebhookEmbedBuilder banEmbedBuilder = new WebhookEmbedBuilder()
                .setColor(0xFF0000)
                .setTitle(new WebhookEmbed.EmbedTitle(String.format("%s вышел с сервера!", Strings.stripColors(event.player.name())), null));
        DiscordWebhookManager.client.send(banEmbedBuilder.build());

        PandorumPlugin.rainbow.remove(p -> p.player.uuid().equals(event.player.uuid()));

        if (PandorumPlugin.currentlyKicking[0] != null && PandorumPlugin.currentlyKicking[0].target().uuid().equals(event.player.uuid())) {
            PandorumPlugin.currentlyKicking[0].stop();
            event.player.getInfo().lastKicked = Time.millis() + VoteKickSession.kickDuration * 1000L;
            sendToChat("commands.votekick.left", event.player.name(), VoteKickSession.kickDuration / 60);
        }

        if(PandorumPlugin.config.type == PluginType.other) return;
        else if(PandorumPlugin.config.type == PluginType.pvp) {
            Seq<String> teamVotes = PandorumPlugin.surrendered.get(event.player.team(), Seq::new);
            if(teamVotes.contains(event.player.uuid())) {
                teamVotes.remove(event.player.uuid());
                int curSurrender = teamVotes.size;
                int reqSurrender = (int)Math.ceil(PandorumPlugin.config.voteRatio * Groups.player.count(p -> p.team() == event.player.team()));
                sendToChat("commands.surrender.left", colorizedTeam(event.player.team()), colorizedName(event.player), curSurrender, reqSurrender);
            }
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
