package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.components.Bundle;
import pandorum.features.Ranks;
import pandorum.features.Ranks.Rank;
import pandorum.database.models.PlayerModel;
import pandorum.util.Utils;

import static pandorum.listeners.handlers.MenuHandler.rankInfoMenu;
import static pandorum.util.Search.findLocale;
import static pandorum.util.Search.findPlayer;

public class RankCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        Player target = args.length > 0 ? findPlayer(args[0]) : player;
        if (target == null) {
            Utils.bundled(player, "commands.player-not-found", args[0]);
            return;
        }

        PlayerModel.find(target, playerModel -> {
            Rank rank = Ranks.getRank(playerModel.rank);
            StringBuilder builder = new StringBuilder(Bundle.format("commands.rank.menu.content", findLocale(player.locale), rank.tag, rank.displayName));

            if (rank.next != null && rank.next.req != null) {
                builder.append(Bundle.format("commands.rank.menu.next",
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

            Call.menu(player.con,
                    rankInfoMenu,
                    Bundle.format("commands.rank.menu.header", findLocale(player.locale), target.coloredName()),
                    builder.toString(),
                    new String[][] {{Bundle.format("ui.menus.close", findLocale(player.locale))}, {Bundle.format("commands.rank.menu.requirements", findLocale(player.locale))}}
            );
        });
    }
}
