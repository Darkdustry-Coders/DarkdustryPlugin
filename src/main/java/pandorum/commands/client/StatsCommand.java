package pandorum.commands.client;

import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.comp.Bundle;
import pandorum.comp.Ranks;
import pandorum.comp.Ranks.Rank;
import pandorum.models.PlayerModel;
import pandorum.util.Utils;

import static pandorum.util.Search.*;
import static pandorum.util.Utils.bundled;

public class StatsCommand {
    public static void run(final String[] args, final Player player) {
        Player target = args.length > 0 ? findPlayer(args[0]) : player;
        if (target == null) {
            bundled(player, "commands.player-not-found", args[0]);
            return;
        }

        PlayerModel.find(target, playerModel -> {
            Rank rank = Ranks.getRank(target, playerModel.rank);
            Call.infoMessage(player.con, Bundle.format("commands.stats.content",
                    findLocale(player.locale),
                    target.coloredName(),
                    rank.tag,
                    rank.name,
                    Utils.millisecondsToMinutes(playerModel.playTime),
                    playerModel.buildingsBuilt,
                    playerModel.buildingsDeconstructed,
                    playerModel.gamesPlayed
            ));
        });
    }
}
