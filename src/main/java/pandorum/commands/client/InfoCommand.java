package pandorum.commands.client;

import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.net.Administration.PlayerInfo;

import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import pandorum.Misc;
import pandorum.comp.Bundle;
import pandorum.models.PlayerModel;

import java.util.concurrent.TimeUnit;

import com.mongodb.BasicDBObject;

import static pandorum.Misc.bundled;
import static pandorum.Misc.findLocale;

public class InfoCommand {
    public static void run(final String[] args, final @NotNull Player player) {
        Player target = args.length > 0 ? Misc.findByName(args[0]) : player;
        if (target == null) {
            bundled(player, "commands.player-not-found");
            return;
        }

        String[][] options = {{
            Bundle.format("events.menu.close", findLocale(player.locale))
        }};

        PlayerModel.find(
            new BasicDBObject("UUID", target.uuid()),
            playerInfo -> {
                Call.menu(
                    player.con,
                    3,
                    Bundle.format(
                        "commands.info.header",
                        findLocale(player.locale),
                        Misc.colorizedName(target),
                        Bundle.format(
                            "commands.info.content",
                            findLocale(player.locale),
                            TimeUnit.MILLISECONDS.toMinutes(playerInfo.playTime),
                            playerInfo.buildingsBuilt,
                            playerInfo.buildingsDeconstructed,
                            playerInfo.wavesSurvived,
                            playerInfo.gamesWon,
                            Bundle.format(
                                "commands.info.hellomsg." + (playerInfo.hellomsg ? "enabled" : "disabled"),
                                findLocale(player.locale)
                            ),
                            Bundle.format(
                                "commands.info.alerts." + (playerInfo.alerts ? "enabled" : "disabled"),
                                findLocale(player.locale)
                            ),
                            playerInfo.locale
                        )
                    ),
                    options
                );
            }
        );
    }
}
