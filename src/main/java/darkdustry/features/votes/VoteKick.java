package darkdustry.features.votes;

import darkdustry.utils.Admins;
import mindustry.gen.*;
import useful.Bundle;

import static darkdustry.PluginVars.*;
import static java.util.concurrent.TimeUnit.*;

public class VoteKick extends VoteSession {

    public final Player started;
    public final Player target;

    public VoteKick(Player started, Player target) {
        this.started = started;
        this.target = target;
    }

    @Override
    public void vote(Player player, int sign) {
        Bundle.send("commands.votekick.vote", player.coloredName(), target.coloredName(), votes() + sign, votesRequired());
        super.vote(player, sign);
    }

    @Override
    public void left(Player player) {
        if (voted.remove(player.id) != 0)
            Bundle.send("commands.votekick.left", player.coloredName(), votes(), votesRequired());

        if (target == player && votes() > 0)
            success();
    }

    @Override
    public void success() {
        stop();
        Bundle.send("commands.votekick.passed", target.coloredName(), MILLISECONDS.toMinutes(kickDuration));
        Admins.kick(target, kickDuration, "kick.vote-kicked", started.coloredName()).kick(kickDuration);
    }

    @Override
    public void fail() {
        stop();
        Bundle.send("commands.votekick.failed", target.coloredName());
    }

    @Override
    public void stop() {
        voteKick = null;
        end.cancel();
    }

    @Override
    public int votesRequired() {
        return Groups.player.size() > 3 ? 3 : 2;
    }
}