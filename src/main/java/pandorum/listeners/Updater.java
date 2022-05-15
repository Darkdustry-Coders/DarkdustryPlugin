package pandorum.listeners;

import mindustry.gen.Groups;
import pandorum.features.Ranks;
import pandorum.mongo.models.MapModel;
import pandorum.mongo.models.PlayerModel;
import pandorum.discord.Bot;

import static mindustry.Vars.state;
import static pandorum.PluginVars.*;

public class Updater implements Runnable {

    public void run() {
        Groups.player.each(player -> PlayerModel.find(player, playerModel -> {
            datas.get(player.uuid()).playTime++;
            playerModel.playTime++;
            playerModel.save();
            Ranks.updateRank(player);
        }));

        MapModel.find(state.map, mapModel -> {
            mapModel.playTime++;
            mapModel.save();
        });

        serverUpTime++;
        mapPlayTime++;

        Bot.updateBotStatus();
    }
}
