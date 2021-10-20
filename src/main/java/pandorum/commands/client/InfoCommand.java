package pandorum.commands.client;

import mindustry.gen.Call;
import mindustry.gen.Player;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import pandorum.Misc;
import pandorum.comp.Bundle;

import java.util.concurrent.TimeUnit;

import static pandorum.Misc.bundled;
import static pandorum.Misc.findLocale;
import static pandorum.PandorumPlugin.createInfo;
import static pandorum.PandorumPlugin.savePlayerStats;

public class InfoCommand {
    public static void run(final String[] args, final @NotNull Player player) {
        Player target = args.length > 0 ? Misc.findByName(args[0]) : player;
        if (target == null) {
            bundled(player, "commands.player-not-found");
            return;
        }

        Document playerInfo = createInfo(target);
        savePlayerStats(target.uuid());

        String[][] options = {{Bundle.format("events.menu.close", findLocale(player.locale))}};

        Call.menu(player.con, 3, Bundle.format("commands.info.header", findLocale(player.locale), Misc.colorizedName(target)),
                Bundle.format("commands.info.content", findLocale(player.locale), TimeUnit.MILLISECONDS.toMinutes(playerInfo.getLong("playtime")),
                playerInfo.getLong("buildingsBuilt"), playerInfo.getLong("buildingsDeconstructed"),
                playerInfo.getInteger("maxWave"), playerInfo.getInteger("gamesPlayed"),
                playerInfo.getBoolean("hellomsg") ? "on" : "off",
                playerInfo.getBoolean("alerts") ? "on" : "off",
                playerInfo.getString("locale")
        ), options);
    }
}
