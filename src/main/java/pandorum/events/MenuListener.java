package pandorum.events;

import mindustry.ui.Menus;
import org.bson.Document;
import pandorum.PandorumPlugin;

public class MenuListener {

    static {
        // Приветственное сообщение
        Menus.registerMenu(1, (player, selection) -> {
            if (selection == 1) {
                Document playerInfo = PandorumPlugin.playersInfo.find((playerInfo2) -> playerInfo2.getString("uuid").equals(player.uuid()));
                playerInfo.replace("hellomsg", false);
                PandorumPlugin.savePlayerStats(player.uuid());
            }
        });
    }
}
