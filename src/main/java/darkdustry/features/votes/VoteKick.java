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
        super();
        this.starter = starter;
        this.target = target;
    }

    @Override
    public void vote(Player player, int sign) {
        super.vote(player, sign);
        sendToChat("commands.votekick.vote", player.name, target.name, votes, votesRequired());
    }

    @Override
    public void success() {
        stop();
        sendToChat("commands.votekick.passed", target.name, kickDuration / 60000);
        kick(target, kickDuration, true, "kick.votekicked", starter.name);
    }

    @Override
    public void fail() {
        stop();
        sendToChat("commands.votekick.failed", target.name);
    }

    @Override
    public int votesRequired() {
        return Groups.player.size() > 4 ? 3 : 2;
    }
}
