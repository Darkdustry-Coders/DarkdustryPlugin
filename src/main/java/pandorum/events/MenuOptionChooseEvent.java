// Я использую MenuOptionChooseEvent вместо Menus.registerMenu.
// Это более надёжный метод. Пожалуйста, не меняйте код этого класса, пока вы на 100% не уверены в том, что делаете.

package pandorum.events;

import mindustry.game.EventType;
import org.bson.Document;
import pandorum.PandorumPlugin;
import static pandorum.Misc.bundled;

public class MenuOptionChooseEvent {
    public static void call(final EventType.MenuOptionChooseEvent event) {
        // Приветственное сообщение
        if (event.menuId == 1 && event.option == 1) {
            Document playerInfo = PandorumPlugin.playersInfo.find((playerInfo2) -> playerInfo2.getString("uuid").equals(event.player.uuid()));
            playerInfo.replace("hellomsg", false);
            PandorumPlugin.savePlayerStats(event.player.uuid());
            bundled(event.player, "events.hellomsg.disabled");
        }
    }
}
