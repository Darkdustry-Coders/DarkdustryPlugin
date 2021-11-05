package pandorum.commands.client;

import com.mongodb.BasicDBObject;
import mindustry.gen.Call;
import mindustry.gen.Player;
import org.jetbrains.annotations.NotNull;
import pandorum.Misc;
import pandorum.comp.Bundle;
import pandorum.comp.Ranks;
import pandorum.models.PlayerModel;

import java.util.concurrent.TimeUnit;

import static pandorum.Misc.bundled;
import static pandorum.Misc.findLocale;

public class InfoCommand implements ClientCommand {
    public static void run(final String[] args, final @NotNull Player player) {
        Player target = args.length > 0 ? Misc.findByName(args[0]) : player;
        if (target == null) {
            bundled(player, "commands.player-not-found");
            return;
        }

        PlayerModel.find(
            new BasicDBObject("UUID", target.uuid()),
            playerInfo -> Call.infoMessage(
                    player.con,
                    Bundle.format(
                            "commands.info.content",
                            findLocale(player.locale),
                            target.coloredName(),
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
                    )
            )
        );
    }
}
