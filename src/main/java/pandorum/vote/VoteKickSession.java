package pandorum.vote;

import arc.util.Timer;
import arc.util.Timer.Task;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.components.Bundle;

import static pandorum.PluginVars.*;
import static pandorum.util.Search.findLocale;
import static pandorum.util.Utils.millisecondsToMinutes;
import static pandorum.util.Utils.sendToChat;

public class VoteKickSession extends VoteSession {

    protected final Player started;
    protected final Player target;

    public VoteKickSession(VoteKickSession[] voteKickSession, Player started, Player target) {
        super(voteKickSession);
        this.started = started;
        this.target = target;
    }

    @Override
    public Task start() {
        return Timer.schedule(() -> {
            if (!checkPass()) {
                sendToChat("commands.votekick.failed", target.coloredName());
                stop();
            }
        }, votekickDuration);
    }

    @Override
    public void vote(Player player, int sign) {
        votes += sign;
        voted.add(player.uuid());
        sendToChat("commands.votekick.vote", player.coloredName(), target.coloredName(), votes, votesRequired());
        checkPass();
    }

    @Override
    public boolean checkPass() {
        if (votes >= votesRequired()) {
            sendToChat("commands.votekick.passed", target.coloredName(), millisecondsToMinutes(kickDuration));
            stop();
            target.kick(Bundle.format("kick.votekicked", findLocale(target.locale), started.coloredName(), millisecondsToMinutes(kickDuration), discordServerUrl), kickDuration);
            return true;
        }
        return false;
    }

    @Override
    public int votesRequired() {
        return Groups.player.size() > 4 ? 3 : 2;
    }

    public Player target() {
        return target;
    }
}
