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
                playerInfo.getInteger("wavesSurvived"), playerInfo.getInteger("gamesWon"),
                (Bundle.format(playerInfo.getBoolean("hellomsg") ? "commands.info.hellomsg.enabled" : "commands.info.hellomsg.disabled", findLocale(player.locale))),
                (Bundle.format(playerInfo.getBoolean("alerts") ? "commands.info.alerts.enabled" : "commands.info.alerts.disabled", findLocale(player.locale))),
                playerInfo.getString("locale")
        ), options);
    }
}
