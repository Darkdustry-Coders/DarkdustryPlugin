package darkdustry.features.menus;

import mindustry.gen.Player;
import useful.Bundle;

import static darkdustry.features.menus.MenuHandler.showMenu;
import static darkdustry.components.MongoDB.*;
import static darkdustry.features.Effects.updateEffects;

public class SettingsMenu {

    public static void showSettingsMenu(Player player) {
        getPlayerData(player).subscribe(data -> showSettingsMenu(player, data));
    }

    public static void showSettingsMenu(Player player, PlayerData data) {
        showMenu(player, "commands.settings.header", "commands.settings.content", new String[][] {
                {button("settings.alerts", player, data.alerts)},
                {button("settings.effects", player, data.effects)},
                {button("settings.doubleTapHistory", player, data.doubleTapHistory)},
                {button("settings.welcomeMessage", player, data.welcomeMessage)},
                {"ui.button.close"}}, SettingsMenu::changeSettings
        );
    }

    public static void changeSettings(Player player, int option) {
        if (option == -1 || option == 4) return; // Меню закрыто

        getPlayerData(player).subscribe(data -> {
            switch (option) {
                case 0 -> data.alerts = !data.alerts;
                case 1 -> {
                    data.effects = !data.effects;
                    updateEffects(player, data);
                }
                case 2 -> data.doubleTapHistory = !data.doubleTapHistory;
                case 3 -> data.welcomeMessage = !data.welcomeMessage;
            }

            setPlayerData(data).subscribe();
            showSettingsMenu(player, data);
        });
    }

    public static String button(String key, Player player, boolean value) {
        return Bundle.format(key, player, Bundle.get(value ? "settings.on" : "settings.off", player));
    }
}