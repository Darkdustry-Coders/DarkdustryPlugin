package darkdustry.features.votes;

import darkdustry.utils.Admins;
import mindustry.gen.*;
import useful.Bundle;

import static darkdustry.PluginVars.*;
import static darkdustry.utils.Checks.*;

public class VoteKick extends VoteSession {

    public final Player initiator, target;
    public final String reason;

    public VoteKick(Player initiator, Player target, String reason) {
        this.initiator = initiator;
        this.target = target;
        this.reason = reason;
    }

    @Override
    public void vote(Player player, int sign) {
        if (alreadyVoted(player, this) || invalidVoteTarget(player, target)) return;

        Bundle.send(sign == 1 ? "commands.votekick.yes" : "commands.votekick.no", player.coloredName(), target.coloredName(), reason, votes() + sign, votesRequired());
        super.vote(player, sign);
    }

    @Override
    public void left(Player player) {
        if (votes.remove(player) != 0)
            Bundle.send("commands.votekick.left", player.coloredName(), votes(), votesRequired());

        if (target == player && votes() > 0)
            success();
    }

    @Override
    public void success() {
        stop();
        Groups.player.each(player -> Bundle.send(player, "commands.votekick.success", target.coloredName(), Bundle.formatDuration(player, kickDuration), reason));

        Admins.voteKick(initiator, target, votes, reason);
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
        return Math.min(3, Groups.player.size());
    }
}