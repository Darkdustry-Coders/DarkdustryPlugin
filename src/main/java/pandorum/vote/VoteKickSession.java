package pandorum.vote;

import arc.struct.Seq;
import arc.util.Timer;
import arc.util.Timer.Task;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Packets.KickReason;

import static pandorum.Misc.millisecondsToMinutes;
import static pandorum.Misc.sendToChat;
import static pandorum.PluginVars.kickDuration;
import static pandorum.PluginVars.votekickDuration;

public class VoteKickSession {
    protected final Player target;
    protected final Seq<String> voted = new Seq<>();
    protected final VoteKickSession[] kickSession;
    protected final Task task;
    protected int votes;

    public VoteKickSession(VoteKickSession[] kickSession, Player target) {
        this.kickSession = kickSession;
        this.target = target;
        this.task = start();
    }

    protected Task start() {
        return Timer.schedule(() -> {
            if (!checkPass()) {
                sendToChat("commands.votekick.failed", target.coloredName());
                stop();
            }
        }, votekickDuration);
    }

    public void vote(Player player, int sign) {
        votes += sign;
        voted.add(player.uuid());
        sendToChat("commands.votekick.vote", player.coloredName(), target.coloredName(), votes, votesRequired());
        checkPass();
    }

    protected boolean checkPass() {
        if (votes >= votesRequired()) {
            sendToChat("commands.votekick.passed", target.coloredName(), millisecondsToMinutes(kickDuration));
            stop();
            target.kick(KickReason.vote, kickDuration);
            return true;
        }
        return false;
    }

    protected int votesRequired() {
        return Groups.player.size() > 4 ? 3 : 2;
    }

    public void stop() {
        voted.clear();
        kickSession[0] = null;
        task.cancel();
    }

    public Seq<String> voted() {
        return voted;
    }

    public Player target() {
        return target;
    }
}
