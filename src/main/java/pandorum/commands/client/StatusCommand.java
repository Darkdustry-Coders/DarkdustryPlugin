package pandorum.commands.client;

import mindustry.gen.Player;
import org.bson.Document;

import java.util.concurrent.TimeUnit;

import static pandorum.Misc.bundled;
import static pandorum.PandorumPlugin.createInfo;
import static pandorum.PandorumPlugin.savePlayerStats;

public class StatusCommand {
    public static void run(final String[] args, final Player player) {
        Document playerInfo = createInfo(player);
        savePlayerStats(player.uuid());
        bundled(player, "commands.status.info", TimeUnit.MILLISECONDS.toMinutes(playerInfo.getLong("playtime")), playerInfo.getLong("buildings"));
    }
}
