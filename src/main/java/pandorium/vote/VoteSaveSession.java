package pandorium.vote;

import static mindustry.Vars.netServer;
import static mindustry.Vars.saveDirectory;
import static mindustry.Vars.saveExtension;
import static pandorium.Misc.sendToChat;
import static pandorium.PandorumPlugin.config;

import arc.util.Timer;
import arc.util.Timer.Task;
import mindustry.gen.Player;
import mindustry.io.SaveIO;

public class VoteSaveSession extends VoteSession{
    private final String target;

    public VoteSaveSession(VoteSession[] map, String target){
        super(map);
        this.target = target;
    }

    @Override
    protected Task start(){
        return Timer.schedule(() -> {
            if(!checkPass()){
                sendToChat("commands.nominate.save.failed", target);
                voted.clear();
                map[0] = null;
                task.cancel();
            }
        }, config.voteDuration);
    }

    @Override
    public void vote(Player player, int d){
        votes += d;
        voted.addAll(player.uuid(), netServer.admins.getInfo(player.uuid()).lastIP);
        sendToChat("commands.nominate.save.vote", player.name, target, votes, votesRequired());
        checkPass();
    }

    @Override
    protected boolean checkPass(){
        if(votes >= votesRequired()){
            sendToChat("commands.nominate.save.passed", target);
            SaveIO.save(saveDirectory.child(String.format("%s.%s", target, saveExtension)));
            map[0] = null;
            task.cancel();
            return true;
        }
        return false;
    }
}
