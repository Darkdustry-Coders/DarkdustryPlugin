package pandorum.listeners;

import mindustry.gen.Groups;
import pandorum.data.PlayerData;
import pandorum.discord.Bot;
import pandorum.features.Ranks;

import static pandorum.PluginVars.*;

public class Updater implements Runnable {

    public void run() {
        Groups.player.each(player -> {
            PlayerData data = datas.get(player.uuid());
            data.playTime++;
            Ranks.updateRank(player);
        });

        serverUpTime++;
        mapPlayTime++;

        Bot.updateBotStatus();
    }
}
