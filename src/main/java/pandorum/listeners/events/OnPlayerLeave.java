package pandorum.listeners.events;

import arc.func.Cons;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.game.EventType.PlayerLeave;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.components.Gamemode;
import pandorum.discord.Bot;
import pandorum.features.Effects;
import pandorum.util.StringUtils;

import java.awt.*;

import static mindustry.Vars.netServer;
import static pandorum.PluginVars.*;
import static pandorum.util.PlayerUtils.sendToChat;
import static pandorum.util.StringUtils.stripAll;

public class OnPlayerLeave implements Cons<PlayerLeave> {

    public void get(PlayerLeave event) {
        Log.info("@ вышел с сервера. [@]", event.player.name, event.player.uuid());
        sendToChat("events.player.leave", event.player.name);
        Bot.sendEmbed(Color.red, "@ покинул сервер.", stripAll(event.player.name));

        activeHistoryPlayers.remove(event.player.uuid());
        activeSpectatingPlayers.remove(event.player.uuid());

        if (currentVoteKick != null && event.player == currentVoteKick.target()) {
            currentVoteKick.stop();
            netServer.admins.handleKicked(event.player.uuid(), event.player.ip(), kickDuration);
            sendToChat("commands.votekick.left", event.player.name, kickDuration / 60000);
        }

        if (config.mode == Gamemode.pvp) {
            Seq<String> teamVotes = votesSurrender.get(event.player.team(), Seq::new);
            if (teamVotes.remove(event.player.uuid())) {
                sendToChat("commands.surrender.left", StringUtils.coloredTeam(event.player.team()), event.player.name, teamVotes.size, Mathf.ceil(voteRatio * Groups.player.count(p -> p.team() == event.player.team())));
            }
        }

        if (votesRtv.remove(event.player.uuid())) {
            sendToChat("commands.rtv.left", event.player.name, votesRtv.size, Mathf.ceil(voteRatio * Groups.player.size()));
        }

        if (votesVnw.remove(event.player.uuid())) {
            sendToChat("commands.vnw.left", event.player.name, votesVnw.size, Mathf.ceil(voteRatio * Groups.player.size()));
        }

        if (!event.player.dead()) Effects.onLeave(event.player.x, event.player.y);

        Seq<Player> players = Groups.player.copy(new Seq<>());
        players.remove(event.player);
        Bot.updateBotStatus(players.size);
    }
}
