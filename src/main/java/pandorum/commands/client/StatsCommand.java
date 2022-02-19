package pandorum.commands.client;

import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.components.Bundle;
import pandorum.components.Ranks;
import pandorum.components.Ranks.Rank;
import pandorum.util.Utils;

import static pandorum.PluginVars.playersInfo;
import static pandorum.util.Search.findLocale;
import static pandorum.util.Search.findPlayer;
import static pandorum.util.Utils.bundled;

public class StatsCommand {
    public static void run(final String[] args, final Player player) {
        Player target = args.length > 0 ? findPlayer(args[0]) : player;
        if (target == null) {
            bundled(player, "commands.player-not-found", args[0]);
            return;
        }

        playersInfo.find(target, playerModel -> {
            Rank rank = Ranks.getRank(target, playerModel.rank);
            Call.infoMessage(player.con, Bundle.format("commands.stats.content",
                    findLocale(player.locale),
                    target.coloredName(),
                    rank.tag,
                    rank.name,
                    Utils.secondsToMinutes(playerModel.playTime),
                    playerModel.buildingsBuilt,
                    playerModel.buildingsDeconstructed,
                    playerModel.gamesPlayed
            ));
        });
    }
}
