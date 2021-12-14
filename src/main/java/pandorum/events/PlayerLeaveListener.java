package pandorum.events;

import arc.struct.Seq;
import arc.util.Log;
import arc.util.Strings;
import discord4j.core.spec.EmbedCreateSpec;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import pandorum.PandorumPlugin;
import pandorum.comp.Config.Gamemode;
import pandorum.comp.Effects;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

import java.util.concurrent.TimeUnit;

import static mindustry.Vars.netServer;
import static pandorum.Misc.colorizedTeam;
import static pandorum.Misc.sendToChat;

public class PlayerLeaveListener {

    public static void call(final EventType.PlayerLeave event) {
        Log.info("@ вышел с сервера, IP: @, ID: @", event.player.getInfo().lastName, event.player.ip(), event.player.uuid());
        sendToChat("events.player.leave", event.player.color, event.player.getInfo().lastName);

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(BotMain.errorColor)
                .title(Strings.format("@ вышел с сервера.", Strings.stripColors(event.player.getInfo().lastName)))
                .build();

        BotHandler.sendEmbed(embed);

        Effects.onLeave(event.player);

        PandorumPlugin.activeHistoryPlayers.remove(event.player.uuid());
        PandorumPlugin.spectating.remove(event.player.uuid());

        if (PandorumPlugin.currentlyKicking[0] != null && event.player == PandorumPlugin.currentlyKicking[0].target()) {
            PandorumPlugin.currentlyKicking[0].stop();
            netServer.admins.handleKicked(event.player.uuid(), event.player.ip(), PandorumPlugin.config.kickDuration);
            sendToChat("commands.votekick.left", event.player.coloredName(), TimeUnit.MILLISECONDS.toMinutes(PandorumPlugin.config.kickDuration));
        }

        if (PandorumPlugin.config.mode == Gamemode.siege || PandorumPlugin.config.mode == Gamemode.pvp) {
            Seq<String> teamVotes = PandorumPlugin.votesSurrender.get(event.player.team(), Seq::new);
            if (teamVotes.remove(event.player.uuid())) {
                sendToChat("commands.surrender.left", colorizedTeam(event.player.team()), event.player.coloredName(), teamVotes.size, (int) Math.ceil(PandorumPlugin.config.voteRatio * Groups.player.count(p -> p.team() == event.player.team())));
            }
        }

        if (PandorumPlugin.votesRTV.remove(event.player.uuid())) {
            sendToChat("commands.rtv.left", event.player.coloredName(), PandorumPlugin.votesRTV.size, (int) Math.ceil(PandorumPlugin.config.voteRatio * Groups.player.size()));
        }

        if (PandorumPlugin.votesVNW.remove(event.player.uuid())) {
            sendToChat("commands.vnw.left", event.player.coloredName(), PandorumPlugin.votesVNW.size, (int) Math.ceil(PandorumPlugin.config.voteRatio * Groups.player.size()));
        }
    }
}
