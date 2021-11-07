package pandorum.vote;

import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Timer;
import arc.util.Timer.Task;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Packets.KickReason;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

import static pandorum.Misc.sendToChat;
import static pandorum.PandorumPlugin.config;

public class VoteKickSession {
    protected Player target;
    protected Seq<String> voted = new Seq<>();
    protected VoteKickSession[] kickSession;
    protected Task task;
    protected int votes;

    public static int kickDuration = 45 * 60;

    public VoteKickSession(VoteKickSession[] kickSession, Player target) {
        this.target = target;
        this.kickSession = kickSession;
        this.task = start();
    }

    public Seq<String> voted() {
        return voted;
    }

    public Player target() {
        return target;
    }

    protected Task start() {
        return Timer.schedule(() -> {
            if (!checkPass()) {
                sendToChat("commands.votekick.vote-failed", target.coloredName());
                stop();
            }
        }, config.votekickDuration);
    }

    public void vote(Player player, int sign) {
        votes += sign;
        voted.add(player.uuid());
        sendToChat("commands.votekick.vote", player.coloredName(), target.coloredName(), votes, votesRequired());
        checkPass();
    }

    protected boolean checkPass() {
        if (votes >= votesRequired()) {
            sendToChat("commands.votekick.vote-passed", target.coloredName(), (kickDuration / 60f));
            Groups.player.each(p -> p.uuid().equals(target.uuid()), p -> p.kick(KickReason.vote, kickDuration * 1000L));
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(BotMain.errorColor)
                    .setAuthor("KICK")
                    .setTitle("Игрок был выгнан с сервера голосованием!")
                    .addField("Никнейм игрока: ", Strings.stripColors(target.name), false);

            BotHandler.botChannel.sendMessage(embed).join();
            stop();
            return true;
        }
        return false;
    }

    public void stop() {
        kickSession[0] = null;
        task.cancel();
        voted.clear();
    }

    protected int votesRequired() {
        return Groups.player.size() > 4 ? 3 : 2;
    }
}
