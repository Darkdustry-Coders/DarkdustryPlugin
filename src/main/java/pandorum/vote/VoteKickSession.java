package pandorum.vote;

import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Timer;
import arc.util.Timer.Task;
import discord4j.core.spec.EmbedCreateSpec;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Packets.KickReason;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

import java.util.concurrent.TimeUnit;

import static pandorum.Misc.sendToChat;
import static pandorum.PandorumPlugin.config;

public class VoteKickSession {
    protected final Player target;
    protected Seq<String> voted = new Seq<>();
    protected VoteKickSession[] kickSession;
    protected Task task;
    protected int votes;

    public VoteKickSession(VoteKickSession[] kickSession, Player target) {
        this.kickSession = kickSession;
        this.target = target;
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
                sendToChat("commands.votekick.failed", target.coloredName());
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
            sendToChat("commands.votekick.passed", target.coloredName(), TimeUnit.MILLISECONDS.toMinutes(config.kickDuration));
            stop();

            target.kick(KickReason.vote, config.kickDuration);

            EmbedCreateSpec embed = EmbedCreateSpec.builder()
                    .color(BotMain.errorColor)
                    .author("KICK", null, "https://thumbs.dreamstime.com/b/red-cross-symbol-icon-as-delete-remove-fail-failure-incorr-incorrect-answer-89999776.jpg")
                    .title("Игрок был выгнан с сервера голосованием!")
                    .addField("Никнейм игрока:", Strings.stripColors(target.name), false)
                    .build();

            BotHandler.sendEmbed(embed);
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
