package pandorum.vote;

import arc.struct.Seq;
import arc.util.Timer;
import arc.util.Timer.Task;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.components.Bundle;
import pandorum.util.Utils;

import static pandorum.PluginVars.*;
import static pandorum.util.Search.findLocale;

public class VoteKickSession {

    protected final Player started;
    protected final Player target;
    protected final Seq<String> voted = new Seq<>();
    protected final Task task;

    protected final VoteKickSession[] voteKickSession;
    protected int votes;

    public VoteKickSession(VoteKickSession[] voteKickSession, Player started, Player target) {
        this.voteKickSession = voteKickSession;
        this.started = started;
        this.target = target;
        this.task = start();
    }

    protected Task start() {
        return Timer.schedule(() -> {
            if (!checkPass()) {
                Utils.sendToChat("commands.votekick.failed", target.coloredName());
                stop();
            }
        }, votekickDuration);
    }

    public void vote(Player player, int sign) {
        votes += sign;
        voted.add(player.uuid());
        Utils.sendToChat("commands.votekick.vote", player.coloredName(), target.coloredName(), votes, votesRequired());
        checkPass();
    }

    protected boolean checkPass() {
        if (votes >= votesRequired()) {
            Utils.sendToChat("commands.votekick.passed", target.coloredName(), Utils.millisecondsToMinutes(kickDuration));
            stop();
            target.kick(Bundle.format("kick.votekicked", findLocale(target.locale), started.coloredName(), Utils.millisecondsToMinutes(kickDuration), discordServerUrl), kickDuration);
            return true;
        }
        return false;
    }

    protected int votesRequired() {
        return Groups.player.size() > 4 ? 3 : 2;
    }

    public void stop() {
        voted.clear();
        task.cancel();
        voteKickSession[0] = null;
    }

    public Seq<String> voted() {
        return voted;
    }

    public Player target() {
        return target;
    }
}
