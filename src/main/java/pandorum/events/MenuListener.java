package pandorum.events;

import mindustry.ui.Menus;
import org.bson.Document;

public class MenuListener {

    static {
        // Приветственное сообщение
        Menus.registerMenu(1, (player, selection) -> {
            if (selection == 1) {
                Document playerInfo = PandorumPlugin.playersInfo.find((playerInfo2) -> playerInfo2.getString("uuid").equals(event.player.uuid()));
                playerInfo.replace("hellomsg", false);
            }
        });
    }
}
