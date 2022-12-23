package darkdustry.features.menus;

import arc.func.Func;
import arc.math.Mathf;
import mindustry.gen.Player;
import useful.Bundle;

import static darkdustry.features.menus.MenuHandler.showMenu;

public class ListMenu {

    public static void show(Player player, String title, Func<Integer, String> content, int page, int pages) {
        showMenu(player, title, content.get(page), new String[][] {
                {"ui.button.left", Bundle.format("ui.button.page", player, page, pages), "ui.button.right"},
                {"ui.button.close"}
        }, option -> {
            switch (option) {
                case 0, 1, 2 -> show(player, title, content, Mathf.clamp(1, page + option - 1, pages), pages);
            }
        });
    }
}