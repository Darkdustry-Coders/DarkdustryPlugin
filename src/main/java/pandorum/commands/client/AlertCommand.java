package pandorum.commands.client;

import mindustry.gen.Player;
import org.bson.Document;

import static pandorum.Misc.bundled;
import static pandorum.PandorumPlugin.createInfo;
import static pandorum.PandorumPlugin.savePlayerStats;

public class AlertCommand {
    public static void run(final String[] args, final Player player) {
        Document playerInfo = createInfo(player);
        if (playerInfo.getBoolean("alerts")) {
            playerInfo.replace("alerts", false);
            bundled(player, "commands.alert.off");
            savePlayerStats(player.uuid());
            return;
        }

        playerInfo.replace("alerts", true);
        bundled(player, "commands.alert.on");
        savePlayerStats(player.uuid());
    }
}
