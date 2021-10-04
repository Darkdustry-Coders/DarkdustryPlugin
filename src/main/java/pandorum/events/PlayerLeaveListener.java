package pandorum.events;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;

import arc.struct.Seq;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import org.bson.Document;
import pandorum.PandorumPlugin;
import pandorum.vote.VoteKickSession;
import pandorum.comp.Config.PluginType;
import pandorum.comp.DiscordWebhookManager;
import pandorum.effects.Effects;

import static pandorum.Misc.colorizedName;
import static pandorum.Misc.sendToChat;
import static pandorum.Misc.colorizedTeam;

public class PlayerLeaveListener {
    public static void call(final EventType.PlayerLeave event) {

        sendToChat("events.player-leave", colorizedName(event.player));
        Log.info("@ вышел с сервера, IP: @, ID: @", event.player.name, event.player.ip(), event.player.uuid());

        Effects.onLeave(event.player);

        if (event.player.con != null) {
            Document playerInfo = PandorumPlugin.createInfo(event.player);
            long time = Time.timeSinceMillis(event.player.con.connectTime) + playerInfo.getLong("playtime");
            playerInfo.replace("playtime", time);
            PandorumPlugin.savePlayerStats(event.player.uuid());
        }

        WebhookEmbedBuilder leaveEmbedBuilder = new WebhookEmbedBuilder()
                .setColor(0xFF0000)
                .setTitle(new WebhookEmbed.EmbedTitle(String.format("%s вышел с сервера!", Strings.stripColors(event.player.name())), null));
        DiscordWebhookManager.client.send(leaveEmbedBuilder.build());

        PandorumPlugin.rainbow.remove(p -> p.player.uuid().equals(event.player.uuid()));
        PandorumPlugin.activeHistoryPlayers.remove(event.player.uuid());

        if (PandorumPlugin.currentlyKicking[0] != null && PandorumPlugin.currentlyKicking[0].target().uuid().equals(event.player.uuid())) {
            PandorumPlugin.currentlyKicking[0].stop();
            event.player.getInfo().lastKicked = Time.millis() + VoteKickSession.kickDuration * 1000L;
            sendToChat("commands.votekick.left", event.player.name(), VoteKickSession.kickDuration / 60f);
        }

        if (PandorumPlugin.config.type == PluginType.other) return;
        if (PandorumPlugin.config.type == PluginType.pvp) {
            Seq<String> teamVotes = PandorumPlugin.surrendered.get(event.player.team(), Seq::new);
            if (teamVotes.contains(event.player.uuid())) {
                teamVotes.remove(event.player.uuid());
                sendToChat("commands.surrender.left", colorizedTeam(event.player.team()), colorizedName(event.player), teamVotes.size, (int)Math.ceil(PandorumPlugin.config.voteRatio * Groups.player.count(p -> p.team() == event.player.team())));
            }
        }

        if (PandorumPlugin.votesRTV.contains(event.player.uuid())) {
            PandorumPlugin.votesRTV.remove(event.player.uuid());
            sendToChat("commands.rtv.left", colorizedName(event.player), PandorumPlugin.votesRTV.size, (int)Math.ceil(PandorumPlugin.config.voteRatio * Groups.player.size()));
        }
        if (PandorumPlugin.votesVNW.contains(event.player.uuid())) {
            PandorumPlugin.votesVNW.remove(event.player.uuid());
            sendToChat("commands.vnw.left", colorizedName(event.player), PandorumPlugin.votesVNW.size, (int)Math.ceil(PandorumPlugin.config.voteRatio * Groups.player.size()));
        }
    }
}
