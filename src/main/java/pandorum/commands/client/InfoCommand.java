package pandorum.commands.client;

import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.comp.Bundle;
import pandorum.comp.Ranks;
import pandorum.comp.Ranks.Rank;
import pandorum.models.PlayerModel;
import pandorum.utils.Utils;

import static pandorum.utils.Search.*;

public class InfoCommand {
    public static void run(final String[] args, final Player player) {
        Player target = args.length > 0 ? findPlayer(args[0]) : player;
        if (target == null) {
            Utils.bundled(player, "commands.player-not-found", args[0]);
            return;
        }

        PlayerModel.find(target.uuid(), playerModel -> {
            Rank rank = Ranks.getRank(target, playerModel.rank);
            Call.infoMessage(player.con, Bundle.format("commands.info.content",
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
