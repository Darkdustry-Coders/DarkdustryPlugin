package pandorum.commands.client;

import com.mongodb.BasicDBObject;
import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.annotations.commands.ClientCommand;
import pandorum.comp.Bundle;
import pandorum.comp.Ranks;
import pandorum.comp.Ranks.Rank;
import pandorum.models.PlayerModel;

import java.util.concurrent.TimeUnit;

import static pandorum.Misc.*;

public class InfoCommand {
    @ClientCommand(name = "info", args = "[player...]", description = "See some info about a player.")
    public static void run(final String[] args, final Player player) {
        Player target = args.length > 0 ? findByName(args[0]) : player;
        if (target == null) {
            bundled(player, "commands.player-not-found");
            return;
        }

        PlayerModel.find(new BasicDBObject("UUID", target.uuid()), playerInfo -> {
            Rank rank = Ranks.getRank(target, playerInfo.rank);
            Call.infoMessage(player.con, Bundle.format("commands.info.content",
                    findLocale(player.locale),
                    target.coloredName(),
                    rank.tag,
                    rank.name,
                    TimeUnit.MILLISECONDS.toMinutes(playerInfo.playTime),
                    playerInfo.buildingsBuilt,
                    playerInfo.buildingsDeconstructed,
                    playerInfo.pvpWinCount,
                    playerInfo.pvpLoseCount,
                    playerInfo.gamesPlayed
            ));
        });
    }
}
