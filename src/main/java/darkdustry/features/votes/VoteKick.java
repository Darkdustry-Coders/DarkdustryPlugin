package darkdustry.features.votes;

import darkdustry.utils.Admins;
import mindustry.gen.*;
import useful.Bundle;

import static darkdustry.PluginVars.*;
import static java.util.concurrent.TimeUnit.*;

public class VoteKick extends VoteSession {

    public final Player player, target;
    public final String reason;

    public VoteKick(Player player, Player target, String reason) {
        this.player = player;
        this.target = target;
        this.reason = reason;
    }

    @Override
    public void vote(Player player, int sign) {
        Bundle.send("commands.votekick.vote", player.coloredName(), target.coloredName(), reason, votes() + sign, votesRequired());
        super.vote(player, sign);
    }

    @Override
    public void left(Player player) {
        if (voted.remove(player) != 0)
            Bundle.send("commands.votekick.left", player.coloredName(), votes(), votesRequired());

        if (target == player && votes() > 0)
            success();
    }

    @Override
    public void success() {
        stop();
        Bundle.send("commands.votekick.success", target.coloredName(), MILLISECONDS.toMinutes(kickDuration), reason);
        Admins.kickReason(target, kickDuration, reason, "kick.vote-kicked", player.coloredName()).kick(kickDuration);
    }

    @Override
    public void fail() {
        stop();
        Bundle.send("commands.votekick.fail", target.coloredName(), reason);
    }

    public void cancel(Player admin) {
        stop();
        Bundle.send("commands.votekick.cancel", admin.coloredName(), target.coloredName(), reason);
    }

    @Override
    public void stop() {
        end.cancel();
        voteKick = null;
    }

    @Override
    public int votesRequired() {
        return Groups.player.size() > 3 ? 3 : 2;
    }
}