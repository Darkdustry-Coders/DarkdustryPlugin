package darkdustry.features.votes;

import mindustry.gen.Groups;
import mindustry.gen.Player;

import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.*;
import static darkdustry.utils.Utils.*;

public class VoteKick extends VoteSession {

    public final Player starter;
    public final Player target;

    public VoteKick(Player starter, Player target) {
        this.starter = starter;
        this.target = target;
    }

    @Override
    public void left(Player player) {
        super.left(player);
        sendToChat("commands.votekick.left", player.coloredName(), votes(), votesRequired());
        if (target == player) success();
    }

    @Override
    public void vote(Player player, int sign) {
        super.vote(player, sign);
        sendToChat("commands.votekick.vote", player.coloredName(), target.coloredName(), votes(), votesRequired());
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
        return Groups.player.size() > 4 ? 3 : 2;
    }
}
