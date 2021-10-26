package pandorum.vote;

import arc.files.Fi;
import arc.util.Log;
import arc.util.Timer;
import arc.util.Timer.Task;
import mindustry.game.Gamemode;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.io.SaveIO;
import mindustry.maps.MapException;
import mindustry.net.WorldReloader;

import static mindustry.Vars.*;
import static pandorum.Misc.colorizedName;
import static pandorum.Misc.sendToChat;
import static pandorum.PandorumPlugin.config;

public class VoteLoadSession extends VoteSession {
    private final Fi target;

    public VoteLoadSession(VoteSession[] session, Fi target) {
        super(session);
        this.target = target;
    }

    @Override
    protected Task start() {
        return Timer.schedule(() -> {
            if (!checkPass()) {
                sendToChat("commands.nominate.load.failed", target.nameWithoutExtension());
                stop();
            }
        }, config.voteDuration);
    }

    @Override
    public void vote(Player player, int sign) {
        votes += sign;
        voted.add(player.uuid());
        sendToChat("commands.nominate.load.vote", colorizedName(player), target.nameWithoutExtension(), votes, votesRequired());
        checkPass();
    }

    @Override
    protected boolean checkPass() {
        if (votes >= votesRequired()) {
            sendToChat("commands.nominate.load.passed", target.nameWithoutExtension());
            stop();

            Runnable r = () -> {
                WorldReloader reloader = new WorldReloader();

                reloader.begin();
                SaveIO.load(target);

                state.rules = state.map.applyRules(Gamemode.survival);
                logic.play();

                reloader.end();
            };

            Timer.schedule(new Task() {
                @Override
                public void run() {
                    try {
                        r.run();
                    } catch(MapException e) {
                        Log.err(e);
                        net.closeServer();
                    }
                }
            }, 10);
            return true;
        }
        return false;
    }

    @Override
    public int votesRequired() {
        return (int)Math.ceil(config.voteRatio * Groups.player.size());
    }
}
