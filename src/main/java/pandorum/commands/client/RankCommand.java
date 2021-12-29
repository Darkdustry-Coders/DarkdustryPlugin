package pandorum.commands.client;

import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.comp.Bundle;
import pandorum.comp.Ranks;
import pandorum.comp.Ranks.Rank;
import pandorum.models.PlayerModel;

import java.util.concurrent.TimeUnit;

import static pandorum.Misc.findLocale;

public class RankCommand {
    public static void run(final String[] args, final Player player) {
        PlayerModel.find(player.uuid(), playerInfo -> {
            Rank rank = Ranks.getRank(player, playerInfo.rank);
            StringBuilder builder = new StringBuilder(Bundle.format("commands.rank.info",
                    findLocale(player.locale),
                    rank.tag,
                    rank.name
            ));

            if (rank.next != null && rank.next.req != null) {
                builder.append(Bundle.format("commands.rank.next",
                        findLocale(player.locale),
                        rank.next.tag,
                        rank.next.name,
                        TimeUnit.MILLISECONDS.toMinutes(playerInfo.playTime),
                        TimeUnit.MILLISECONDS.toMinutes(rank.next.req.playtime),
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
