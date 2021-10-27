package pandorum.commands.client;

import static pandorum.Misc.bundled;
import static pandorum.Misc.findLocale;

import java.util.concurrent.TimeUnit;

import com.mongodb.BasicDBObject;

import org.jetbrains.annotations.NotNull;

import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.Misc;
import pandorum.comp.Bundle;
import pandorum.comp.Ranks;
import pandorum.models.PlayerModel;

public class InfoCommand implements ClientCommand {
    public static void run(final String[] args, final @NotNull Player player) {
        Player target = args.length > 0 ? Misc.findByName(args[0]) : player;
        if (target == null) {
            bundled(player, "commands.player-not-found");
            return;
        }

        String[][] options = {{Bundle.format("events.menu.close", findLocale(player.locale))}};

        PlayerModel.find(
            new BasicDBObject("UUID", target.uuid()),
            playerInfo -> {
                Call.menu(
                        player.con,
                        3,
                        Bundle.format(
                                "commands.info.header",
                                findLocale(player.locale),
                                Misc.colorizedName(target)
                        ),
                        Bundle.format(
                                "commands.info.content",
                                findLocale(player.locale),
                                Ranks.ranks.get(playerInfo.rank).tag,
                                Ranks.ranks.get(playerInfo.rank).name,
                                TimeUnit.MILLISECONDS.toMinutes(playerInfo.playTime),
                                playerInfo.buildingsBuilt,
                                playerInfo.buildingsDeconstructed,
                                playerInfo.maxWave,
                                playerInfo.gamesPlayed,
                                playerInfo.hellomsg ? "on" : "off",
                                playerInfo.alerts ? "on" : "off",
                                playerInfo.locale
                        ),
                        options
                );
            }
        );
    }
}
