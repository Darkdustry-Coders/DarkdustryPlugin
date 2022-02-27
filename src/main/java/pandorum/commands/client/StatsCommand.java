package pandorum.commands.client;

import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.components.Bundle;
import pandorum.components.Ranks;
import pandorum.components.Ranks.Rank;
import pandorum.database.models.PlayerModel;
import pandorum.util.Utils;

import static pandorum.events.handlers.MenuHandler.statsMenu;
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

        PlayerModel.find(target, playerModel -> {
            Rank rank = Ranks.getRank(playerModel.rank);

            Call.menu(player.con,
                    statsMenu,
                    Bundle.format("commands.stats.menu.header", findLocale(player.locale), target.coloredName()),
                    Bundle.format("commands.stats.menu.content", findLocale(player.locale), rank.tag, rank.displayName, Utils.secondsToMinutes(playerModel.playTime), playerModel.buildingsBuilt, playerModel.buildingsDeconstructed, playerModel.gamesPlayed),
                    new String[][] {{Bundle.format("ui.menus.close", findLocale(player.locale))}}
            );
        });
    }
}
