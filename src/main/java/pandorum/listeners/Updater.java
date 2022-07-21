package pandorum.listeners;

import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.components.Bundle;
import pandorum.data.PlayerData;
import pandorum.features.Ranks.Rank;

import static pandorum.PluginVars.mapPlayTime;
import static pandorum.PluginVars.serverUpTime;
import static pandorum.data.Database.getPlayerData;
import static pandorum.data.Database.setPlayerData;
import static pandorum.features.Ranks.getRank;
import static pandorum.listeners.handlers.MenuHandler.rankIncreaseMenu;
import static pandorum.util.Search.findLocale;

public class Updater implements Runnable {

    public void run() {
        Groups.player.each(this::updatePlayer);

        serverUpTime++;
        mapPlayTime++;
    }

    // TODO кринж метод, упростить или убрать
    public void updatePlayer(Player player) {
        PlayerData data = getPlayerData(player.uuid());
        data.playTime++;

        Rank rank = getRank(data.rank);
        if (rank.checkNext(data.playTime, data.buildingsBuilt, data.gamesPlayed)) {
            rank = rank.next;
            data.rank = rank.id;

            Call.menu(player.con, rankIncreaseMenu,
                    Bundle.format("events.rank-increase.menu.header", findLocale(player.locale)),
                    Bundle.format("events.rank-increase.menu.content", findLocale(player.locale), rank.tag, rank.displayName, data.playTime / 60, data.buildingsBuilt, data.gamesPlayed),
                    new String[][] {{Bundle.format("ui.menus.close", findLocale(player.locale))}}
            );
        }

        player.name(rank.tag + "[#" + player.color + "]" + player.getInfo().lastName);

        setPlayerData(player.uuid(), data);
    }
}
