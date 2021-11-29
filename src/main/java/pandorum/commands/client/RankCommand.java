package pandorum.commands.client;

import com.mongodb.BasicDBObject;
import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.comp.Bundle;
import pandorum.models.PlayerModel;
import pandorum.comp.Ranks;
import pandorum.comp.Ranks.Rank;

import java.util.concurrent.TimeUnit;

import static pandorum.Misc.findLocale;

public class RankCommand {
    public static void run(final String[] args, final Player player) {
        PlayerModel.find(new BasicDBObject("UUID", player.uuid()), playerInfo -> {
            Rank rank = Ranks.get(playerInfo.rank);
            StringBuilder builder = new StringBuilder(Bundle.format("commands.rank.info",
                    findLocale(player.locale),
                    rank.tag,
                    rank.name)
            );

            if (rank.next != null && rank.nextReq != null) {
                builder.append(Bundle.format("commands.rank.next",
                        findLocale(player.locale),
                        rank.next.tag,
                        rank.next.name,
                        TimeUnit.MILLISECONDS.toMinutes(playerInfo.playTime),
                        TimeUnit.MILLISECONDS.toMinutes(rank.nextReq.playtime),
                        playerInfo.buildingsBuilt,
                        rank.nextReq.buildingsBuilt,
                        playerInfo.gamesPlayed,
                        rank.nextReq.gamesPlayed)
                );
            }

            Call.infoMessage(player.con, builder.toString());
        });
    }
}
