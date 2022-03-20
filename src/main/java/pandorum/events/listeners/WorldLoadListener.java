package pandorum.events.listeners;

import arc.util.Time;

import static pandorum.PluginVars.*;
import static pandorum.util.Utils.sendToChat;

public class WorldLoadListener implements Runnable {

    public void run() {
        votesSurrender.clear();
        votesRtv.clear();
        votesVnw.clear();

        mapRateVotes.clear();

        history.clear();

        mapPlayTime = 0;
        canVote = true;

        Time.run(600f, () -> sendToChat("events.world-loaded"));
    }
}
