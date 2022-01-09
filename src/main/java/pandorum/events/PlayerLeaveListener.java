package pandorum.events;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Strings;
import discord4j.core.spec.EmbedCreateSpec;
import mindustry.game.EventType.PlayerLeave;
import mindustry.gen.Groups;
import pandorum.comp.Config.Gamemode;
import pandorum.comp.Effects;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

import static mindustry.Vars.netServer;
import static pandorum.Misc.*;
import static pandorum.PluginVars.*;

public class PlayerLeaveListener {

    public static void call(final PlayerLeave event) {
        Log.info("@ вышел с сервера. [@]", event.player.name, event.player.uuid());
        sendToChat("events.player.leave", event.player.coloredName());
        BotHandler.sendEmbed(EmbedCreateSpec.builder().color(BotMain.errorColor).title(Strings.format("@ вышел с сервера.", Strings.stripColors(event.player.name))).build());

        if (!event.player.dead()) Effects.onLeave(event.player.x, event.player.y);

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

        if (votesRTV.remove(event.player.uuid())) {
            sendToChat("commands.rtv.left", event.player.coloredName(), votesRTV.size, Mathf.ceil(voteRatio * Groups.player.size()));
        }

        if (votesVNW.remove(event.player.uuid())) {
            sendToChat("commands.vnw.left", event.player.coloredName(), votesVNW.size, Mathf.ceil(voteRatio * Groups.player.size()));
        }
    }
}
