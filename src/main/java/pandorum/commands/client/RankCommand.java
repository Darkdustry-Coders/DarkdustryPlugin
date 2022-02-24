package pandorum.commands.client;

import arc.util.Strings;
import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.components.Bundle;
import pandorum.components.Ranks;
import pandorum.components.Ranks.Rank;
import pandorum.database.models.PlayerModel;
import pandorum.util.Utils;

import static pandorum.util.Search.findLocale;
import static pandorum.util.Search.findPlayer;

public class RankCommand {
    public static void run(final String[] args, final Player player) {
        Player target = args.length > 0 ? findPlayer(args[0]) : player;
        if (target == null) {
            Utils.bundled(player, "commands.player-not-found", args[0]);
            return;
        }

        PlayerModel.find(target, playerModel -> {
            Rank rank = Ranks.getRank(playerModel.rank);
            StringBuilder builder = new StringBuilder(Bundle.format("commands.rank", findLocale(player.locale), target.coloredName(), rank.tag, rank.displayName));

            if (rank.next != null && rank.next.req != null) {
                builder.append(Bundle.format("commands.rank.next",
                        findLocale(player.locale),
                        rank.next.tag,
                        rank.next.displayName,
                        Utils.secondsToMinutes(playerModel.playTime),
                        Utils.secondsToMinutes(rank.next.req.playTime),
                        playerModel.buildingsBuilt,
                        rank.next.req.buildingsBuilt,
                        playerModel.gamesPlayed,
                        rank.next.req.gamesPlayed
                ));
            }

            builder.append(Bundle.format("commands.rank.ranks", findLocale(player.locale)));
            for (Rank r : Rank.ranks) {
                builder.append(Strings.format("\n[lightgray] - @[cyan]@", rank.tag, rank.displayName));
                if (r.req != null) {
                    builder.append(Bundle.format("commands.rank.requirements", findLocale(player.locale), Utils.secondsToMinutes(r.req.playTime), r.req.buildingsBuilt, r.req.gamesPlayed));
                }
            }

            Call.infoMessage(player.con, builder.toString());
        });
    }
}
