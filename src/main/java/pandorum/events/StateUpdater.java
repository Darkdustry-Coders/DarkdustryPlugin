package pandorum.events;

import mindustry.gen.Groups;
import pandorum.components.Ranks;
import pandorum.database.models.MapModel;
import pandorum.database.models.PlayerModel;
import pandorum.discord.Bot;

import static mindustry.Vars.state;
import static pandorum.PluginVars.mapPlayTime;
import static pandorum.PluginVars.serverUpTime;

public class StateUpdater {

    public static void update() {
        Groups.player.each(player -> PlayerModel.find(player, playerModel -> {
            playerModel.playTime++;
            playerModel.save();
            Ranks.updateName(player);
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
