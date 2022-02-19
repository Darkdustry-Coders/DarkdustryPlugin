package pandorum.events;

import mindustry.gen.Groups;
import pandorum.components.Ranks;
import pandorum.discord.Bot;

import static mindustry.Vars.state;
import static pandorum.PluginVars.*;

public class StateUpdater {

    public static void update() {
        Groups.player.each(player -> playersInfo.find(player, playerModel -> {
            playerModel.playTime++;
            playerModel.save();
            Ranks.updateName(player);
        }));

        mapsInfo.find(state.map, mapModel -> {
            mapModel.playTime++;
            mapModel.save();
        });

        serverUpTime++;
        mapPlayTime++;

        Bot.updateBotStatus();
    }
}
