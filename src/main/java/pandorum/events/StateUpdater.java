package pandorum.events;

import mindustry.gen.Groups;
import pandorum.comp.Ranks;
import pandorum.discord.Bot;
import pandorum.models.MapModel;
import pandorum.models.PlayerModel;

import static mindustry.Vars.state;
import static pandorum.PluginVars.*;

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