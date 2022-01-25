package pandorum.events.listeners;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import mindustry.game.EventType.PlayerLeave;
import mindustry.gen.Groups;
import net.dv8tion.jda.api.EmbedBuilder;
import pandorum.comp.Config.Gamemode;
import pandorum.comp.Effects;
import pandorum.discord.Bot;

import java.awt.*;

import static mindustry.Vars.netServer;
import static pandorum.Misc.*;
import static pandorum.PluginVars.*;
import static pandorum.discord.Bot.botChannel;

public class PlayerLeaveListener {

    public static void call(final PlayerLeave event) {
        Log.info("@ вышел с сервера. [@]", event.player.name, event.player.uuid());
        sendToChat("events.player.leave", event.player.coloredName());
        botChannel.sendMessageEmbeds(new EmbedBuilder().setTitle(Strings.format("@ вышел с сервера.", Strings.stripColors(event.player.name))).setColor(Color.red).build()).queue();

        if (!event.player.dead()) Effects.onLeave(event.player.x, event.player.y);

        updateTimers.remove(event.player.uuid()).cancel();
        activeHistoryPlayers.remove(event.player.uuid());
        activeSpectatingPlayers.remove(event.player.uuid());

        if (currentVotekick[0] != null && event.player == currentVotekick[0].target()) {
            currentVotekick[0].stop();
            netServer.admins.handleKicked(event.player.uuid(), event.player.ip(), kickDuration);
            sendToChat("commands.votekick.left", event.player.coloredName(), millisecondsToMinutes(kickDuration));
        }

        if (config.mode == Gamemode.pvp) {
            Seq<String> teamVotes = votesSurrender.get(event.player.team(), Seq::new);
            if (teamVotes.remove(event.player.uuid())) {
                sendToChat("commands.surrender.left", colorizedTeam(event.player.team()), event.player.coloredName(), teamVotes.size, Mathf.ceil(voteRatio * Groups.player.count(p -> p.team() == event.player.team())));
            }
        }

        if (votesRtv.remove(event.player.uuid())) {
            sendToChat("commands.rtv.left", event.player.coloredName(), votesRtv.size, Mathf.ceil(voteRatio * Groups.player.size()));
        }

        if (votesVnw.remove(event.player.uuid())) {
            sendToChat("commands.vnw.left", event.player.coloredName(), votesVnw.size, Mathf.ceil(voteRatio * Groups.player.size()));
        }

        Time.runTask(30f, Bot::updateBotStatus);
    }
}
