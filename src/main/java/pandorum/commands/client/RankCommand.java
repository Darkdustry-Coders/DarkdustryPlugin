package pandorum.commands.client;

import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.comp.Bundle;
import pandorum.comp.Ranks;
import pandorum.comp.Ranks.Rank;
import pandorum.models.PlayerModel;

import static pandorum.Misc.*;
import static pandorum.Misc.bundled;

public class RankCommand {
    public static void run(final String[] args, final Player player) {
        Player target = args.length > 0 ? findPlayer(args[0]) : player;
        if (target == null) {
            bundled(player, "commands.player-not-found", args[0]);
            return;
        }

        PlayerModel.find(target.uuid(), playerInfo -> {
            Rank rank = Ranks.getRank(target, playerInfo.rank);
            StringBuilder builder = new StringBuilder(Bundle.format("commands.rank.info",
                    findLocale(player.locale),
                    target.coloredName(),
                    rank.tag,
                    rank.name
            ));

            if (rank.next != null && rank.next.req != null) {
                builder.append(Bundle.format("commands.rank.next",
                        findLocale(player.locale),
                        rank.next.tag,
                        rank.next.name,
                        millisecondsToMinutes(playerInfo.playTime),
                        millisecondsToMinutes(rank.next.req.playtime),
                        playerInfo.buildingsBuilt,
                        rank.next.req.buildingsBuilt,
                        playerInfo.gamesPlayed,
                        rank.next.req.gamesPlayed
                ));
            }

            Call.infoMessage(player.con, builder.toString());
        });
    }
}
