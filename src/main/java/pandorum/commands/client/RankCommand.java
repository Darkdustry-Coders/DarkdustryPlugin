package pandorum.commands.client;

import com.mongodb.BasicDBObject;
import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.Misc;
import pandorum.comp.Bundle;
import pandorum.models.PlayerModel;
import pandorum.ranks.Ranks;

import java.util.concurrent.TimeUnit;

public class RankCommand implements ClientCommand {
    public static void run(final String[] args, final Player player) {
        PlayerModel.find(new BasicDBObject("UUID", player.uuid()), playerInfo -> Ranks.getRank(player, rank -> Call.infoMessage(player.con, Bundle.format("commands.rank.info",
                Misc.findLocale(player.locale),
                rank.tag,
                rank.name)
        )));
    }
}
