package pandorum.events;

import mindustry.gen.Groups;
import pandorum.components.Ranks;
import pandorum.database.databridges.MapInfo;
import pandorum.database.databridges.PlayerInfo;
import pandorum.discord.Bot;

import static mindustry.Vars.state;
import static pandorum.PluginVars.mapPlayTime;
import static pandorum.PluginVars.serverUpTime;

public class StateUpdater {

    public static void update() {
        Groups.player.each(player -> PlayerInfo.find(player, playerModel -> {
            playerModel.playTime++;
            PlayerInfo.save(playerModel);
            Ranks.updateName(player);
        }));

        MapInfo.find(state.map, mapModel -> {
            mapModel.playTime++;
            MapInfo.save(mapModel);
        });

        serverUpTime++;
        mapPlayTime++;

        Bot.updateBotStatus();
    }
}
