package pandorum.vote;

import static pandorum.PandorumPlugin.config;
import static pandorum.Misc.sendToChat;

import arc.struct.ObjectSet;
import arc.util.Timer;
import arc.util.Timer.Task;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.Vars;
import mindustry.net.Packets.KickReason;

public class VoteKickSession{
    protected Player target;
    protected ObjectSet<String> voted = new ObjectSet<>();
    protected VoteKickSession[] map;
    protected Task task;
    protected int votes;

    public static int kickDuration = 30 * 60;

    public VoteKickSession(VoteKickSession[] map, Player target) {         
        this.target = target;
        this.map = map;
        this.task = start();
    }

    public ObjectSet<String> voted() {
        return voted;
    }

    public Player target() {
        return target;
    }

    protected Task start() {
        return Timer.schedule(() -> {
            if(!checkPass()) {
                sendToChat("commands.votekick.vote-failed", target.name);
                voted.clear();
                map[0] = null;
                task.cancel();
            }
        }, config.votekickDuration);
    }

    public void vote(Player player, int d){
        votes += d;
        voted.addAll(player.uuid(), Vars.netServer.admins.getInfo(player.uuid()).lastIP);
        sendToChat("commands.votekick.vote", player.name, target.name, votes, votesRequired());
        checkPass();
    }

    protected boolean checkPass(){
        if(votes >= votesRequired()){
            sendToChat("commands.votekick.vote-passed", target.name, (kickDuration / 60));
            Groups.player.each(p -> p.uuid().equals(target.uuid()), p -> p.kick(KickReason.vote, kickDuration * 1000));
            map[0] = null;
            task.cancel();
            return true;
        }
        return false;
    }

    public void stop() {
        map[0] = null;
        task.cancel();
        voted.clear();
    }

    protected int votesRequired(){
        return 2 + (Groups.player.size() > 4 ? 1 : 0);
    }
}
