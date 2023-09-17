package darkdustry.features.votes;

import darkdustry.components.Socket;
import darkdustry.listeners.SocketEvents.VoteKickEvent;
import darkdustry.utils.Admins;
import mindustry.gen.*;
import useful.Bundle;

import static arc.util.Strings.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Config.*;

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
        Bundle.send(sign == 1 ? "commands.votekick.yes" : "commands.votekick.no", player.coloredName(), target.coloredName(), reason, votes() + sign, votesRequired());
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
        Groups.player.each(player -> Bundle.send(player, "commands.votekick.success", target.coloredName(), Bundle.formatDuration(player, kickDuration), reason));

        var votesFor = new StringBuilder();
        var votesAgainst = new StringBuilder();

        voted.forEach(entry -> {
            switch (entry.value) {
                case 1 -> votesFor.append("[scarlet]- ").append(entry.key.coloredName()).append("\n");
                case -1 -> votesAgainst.append("[scarlet]- ").append(entry.key.coloredName()).append("\n");
            }
        });

        if (votesFor.isEmpty()) votesFor.append("<none>").append("\n");
        if (votesAgainst.isEmpty()) votesAgainst.append("<none>").append("\n");

        Admins.kickReason(target, kickDuration, reason, "kick.vote-kicked", initiator.coloredName(), votesFor, votesAgainst).kick(kickDuration);
        Socket.send(new VoteKickEvent(config.mode.name(), initiator.plainName(), target.plainName(), reason, stripColors(votesFor), stripColors(votesAgainst)));
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