package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.components.Bundle;
import pandorum.data.PlayerData;
import pandorum.features.Ranks;
import pandorum.features.Ranks.Rank;

import static pandorum.data.Database.getPlayerData;
import static pandorum.listeners.handlers.MenuHandler.rankInfoMenu;
import static pandorum.util.PlayerUtils.bundled;
import static pandorum.util.Search.findLocale;
import static pandorum.util.Search.findPlayer;

public class RankCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        Player target = args.length > 0 ? findPlayer(args[0]) : player;
        if (target == null) {
            bundled(player, "commands.player-not-found", args[0]);
            return;
        }

        PlayerData data = getPlayerData(target.uuid());
        Rank rank = Ranks.getRank(data.rank), next = rank.next;

        StringBuilder builder = new StringBuilder(Bundle.format("commands.rank.menu.content", findLocale(player.locale), rank.tag, rank.displayName));

        if (next != null && next.req != null) {
            builder.append(Bundle.format("commands.rank.menu.next",
                    findLocale(player.locale),
                    next.tag,
                    next.displayName,
                    data.playTime / 60,
                    next.req.playTime / 60,
                    data.buildingsBuilt,
                    next.req.buildingsBuilt,
                    data.gamesPlayed,
                    next.req.gamesPlayed
            ));
        }

        Call.menu(player.con, rankInfoMenu,
                Bundle.format("commands.rank.menu.header", findLocale(player.locale), target.coloredName()),
                builder.toString(),
                new String[][] {{Bundle.format("ui.menus.close", findLocale(player.locale))}, {Bundle.format("commands.rank.menu.requirements", findLocale(player.locale))}}
        );
    }
}
