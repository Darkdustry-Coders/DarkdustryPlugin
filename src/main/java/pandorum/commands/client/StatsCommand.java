package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.components.Bundle;
import pandorum.data.PlayerData;
import pandorum.features.Ranks.Rank;
import pandorum.util.Utils;

import static pandorum.PluginVars.datas;
import static pandorum.listeners.handlers.MenuHandler.statsMenu;
import static pandorum.util.Search.findLocale;
import static pandorum.util.Search.findPlayer;
import static pandorum.util.Utils.bundled;

public class StatsCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        Player target = args.length > 0 ? findPlayer(args[0]) : player;
        if (target == null) {
            bundled(player, "commands.player-not-found", args[0]);
            return;
        }

        PlayerData data = datas.get(target.uuid());
        Rank rank = data.rank;

        Call.menu(player.con, statsMenu,
                Bundle.format("commands.stats.menu.header", findLocale(player.locale), target.coloredName()),
                Bundle.format("commands.stats.menu.content", findLocale(player.locale), rank.tag, rank.displayName, Utils.secondsToMinutes(data.playTime), data.buildingsBuilt, data.gamesPlayed),
                new String[][] {{Bundle.format("ui.menus.close", findLocale(player.locale))}}
        );
    }
}
