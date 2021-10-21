package pandorum.events;

import arc.struct.Seq;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import com.mongodb.BasicDBObject;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import net.dv8tion.jda.api.EmbedBuilder;
import pandorum.PandorumPlugin;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;
import pandorum.models.PlayerModel;
import pandorum.vote.VoteKickSession;
import pandorum.comp.Config.Gamemode;
import pandorum.effects.Effects;

import static pandorum.Misc.sendToChat;
import static pandorum.Misc.colorizedTeam;

public class PlayerLeaveListener {
    public static void call(final EventType.PlayerLeave event) {

        sendToChat("events.player-leave", event.player.coloredName());
        Log.info("@ вышел с сервера, IP: @, ID: @", event.player.name, event.player.ip(), event.player.uuid());

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(BotMain.errorColor)
                .setTitle(Strings.format("**@** вышел с сервера!", Strings.stripColors(event.player.name)));

        BotHandler.botChannel.sendMessageEmbeds(embed.build()).queue();

        Effects.onLeave(event.player);

        PlayerModel.find(new BasicDBObject("UUID", event.player.uuid()), playerInfo -> playerInfo.playTime += Time.timeSinceMillis(event.player.con.connectTime));

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
