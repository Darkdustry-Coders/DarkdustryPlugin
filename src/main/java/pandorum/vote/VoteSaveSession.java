package pandorum.vote;

import arc.util.Strings;
import arc.util.Timer;
import arc.util.Timer.Task;
import mindustry.gen.Player;
import mindustry.io.SaveIO;

import static mindustry.Vars.saveDirectory;
import static mindustry.Vars.saveExtension;
import static pandorum.Misc.sendToChat;
import static pandorum.PandorumPlugin.config;

public class VoteSaveSession extends VoteSession {
    protected final String target;

    public VoteSaveSession(VoteSession[] session, String target) {
        super(session);
        this.target = target;
    }

    @Override
    protected Task start() {
        return Timer.schedule(() -> {
            if (!checkPass()){
                sendToChat("commands.nominate.save.failed", target);
                stop();
            }
        }, config.voteDuration);
    }

    @Override
    public void vote(Player player, int sign) {
        votes += sign;
        voted.add(player.uuid());
        sendToChat("commands.nominate.save.vote", player.coloredName(), target, votes, votesRequired());
        checkPass();
    }

    @Override
    protected boolean checkPass() {
        if (votes >= votesRequired()) {
            sendToChat("commands.nominate.save.passed", target);
            stop();
            SaveIO.save(saveDirectory.child(Strings.format("@.@", target, saveExtension)));
        }
        return false;
    }
}
