package pandorum.events;

import arc.struct.Seq;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.object.presence.Status;
import discord4j.core.spec.EmbedCreateSpec;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import pandorum.PandorumPlugin;
import pandorum.comp.Config.Gamemode;
import pandorum.comp.Effects;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;
import pandorum.vote.VoteKickSession;

import static pandorum.Misc.colorizedTeam;
import static pandorum.Misc.sendToChat;

public class PlayerLeaveListener {
    public static void call(final EventType.PlayerLeave event) {
        Log.info("@ вышел с сервера, IP: @, ID: @", event.player.getInfo().lastName, event.player.ip(), event.player.uuid());
        sendToChat("events.player-leave", event.player.color.toString().toUpperCase(), event.player.getInfo().lastName);

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(BotMain.errorColor)
                .title(Strings.format("@ вышел с сервера.", Strings.stripColors(event.player.getInfo().lastName)))
                .build();

        BotHandler.sendEmbed(embed);

        Effects.onLeave(event.player);

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
                sendToChat("commands.surrender.left", colorizedTeam(event.player.team()), event.player.coloredName(), teamVotes.size, (int) Math.ceil(PandorumPlugin.config.voteRatio * Groups.player.count(p -> p.team() == event.player.team())));
            }
        }

        if (PandorumPlugin.votesRTV.contains(event.player.uuid())) {
            PandorumPlugin.votesRTV.remove(event.player.uuid());
            sendToChat("commands.rtv.left", event.player.coloredName(), PandorumPlugin.votesRTV.size, (int) Math.ceil(PandorumPlugin.config.voteRatio * Groups.player.size()));
        }

        if (PandorumPlugin.votesVNW.contains(event.player.uuid())) {
            PandorumPlugin.votesVNW.remove(event.player.uuid());
            sendToChat("commands.vnw.left", event.player.coloredName(), PandorumPlugin.votesVNW.size, (int) Math.ceil(PandorumPlugin.config.voteRatio * Groups.player.size()));
        }

        BotMain.client.updatePresence(ClientPresence.of(Status.ONLINE, ClientActivity.watching("Игроков на сервере: " + Groups.player.size()))).subscribe(null, e -> {});
    }
}
