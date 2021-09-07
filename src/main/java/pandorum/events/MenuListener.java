package pandorum.events;

import mindustry.ui.Menus;

public class MenuListener {

    static {
        // Приветственное сообщение
        Menus.registerMenu(1, (player, selection) -> {
            if (selection == 0) {
                // Отключаем приветственное сообщение
            }
        });
    }
}
