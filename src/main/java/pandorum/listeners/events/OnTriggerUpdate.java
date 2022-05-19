package pandorum.listeners.events;

import mindustry.gen.Groups;
import pandorum.features.Effects;

import static pandorum.PluginVars.*;
import static pandorum.data.Database.setPlayerData;

public class OnTriggerUpdate implements Runnable {

    public void run() {
        Groups.player.each(player -> player.unit().moving(), player -> Effects.onMove(player.x, player.y));

        if (interval.get(1, databaseSaveTimer)) {
            Groups.player.each(player -> setPlayerData(player.uuid(), datas.get(player.uuid())));
        }
    }
}
