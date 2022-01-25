package pandorum.vote;

import arc.struct.Seq;
import arc.util.Timer;
import arc.util.Timer.Task;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.comp.Bundle;
import pandorum.utils.Utils;

import static pandorum.utils.Search.*;
import static pandorum.PluginVars.*;

public class VoteKickSession {

    protected final Player player;
    protected final Player target;
    protected final Seq<String> voted = new Seq<>();
    protected final VoteKickSession[] kickSession;
    protected final Task task;
    protected int votes;

    public VoteKickSession(VoteKickSession[] kickSession, Player player, Player target) {
        this.kickSession = kickSession;
        this.player = player;
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
            target.kick(Bundle.format("kick.votekicked", findLocale(target.locale), player.coloredName(), Utils.millisecondsToMinutes(kickDuration), discordServerUrl), kickDuration);
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
