package darkdustry.features.votes;

import mindustry.gen.*;

import static darkdustry.PluginVars.*;
import static darkdustry.utils.Administration.kick;
import static useful.Bundle.sendToChat;

public class VoteKick extends VoteSession {

    public final Player starter;
    public final Player target;

    public VoteKick(Player starter, Player target) {
        this.starter = starter;
        this.target = target;
    }

    @Override
    public void vote(Player player, int sign) {
        super.vote(player, sign);
        sendToChat("commands.votekick.vote", player.coloredName(), target.coloredName(), votes(), votesRequired());
    }

    @Override
    public void left(Player player) {
        if (voted.remove(player.id) != 0)
            sendToChat("commands.votekick.left", player.coloredName(), votes(), votesRequired());

        if (target == player && votes() > 0)
            success();
    }

    @Override
    public void success() {
        stop();
        sendToChat("commands.votekick.passed", target.coloredName(), kickDuration / 60000);
        kick(target, kickDuration, true, "kick.votekicked", starter.coloredName());
    }

    @Override
    public void fail() {
        stop();
        sendToChat("commands.votekick.failed", target.coloredName());
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