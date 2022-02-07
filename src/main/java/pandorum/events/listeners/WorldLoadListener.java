package pandorum.events.listeners;

import arc.util.Timer;

import static pandorum.PluginVars.*;
import static pandorum.util.Utils.sendToChat;

public class WorldLoadListener {

    public static void call() {
        votesSurrender.clear();
        votesRtv.clear();
        votesVnw.clear();

        mapRateVotes.clear();

        history.clear();

        mapPlayTime = 0;
        canVote = true;

        if (worldLoadTask != null) worldLoadTask.cancel();
        worldLoadTask = Timer.schedule(() -> sendToChat("events.world-loaded"), 10f);
    }
}
