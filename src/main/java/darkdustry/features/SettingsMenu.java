package darkdustry.features;

import darkdustry.utils.Find;
import mindustry.gen.Player;

import java.util.Locale;

import static darkdustry.components.Bundle.*;
import static darkdustry.components.MenuHandler.*;
import static darkdustry.components.MongoDB.*;
import static darkdustry.features.Effects.updateEffects;

public class SettingsMenu {

    public static void showSettingsMenu(Player player) {
        getPlayerData(player.uuid()).subscribe(data -> showSettingsMenu(player, data));
    }

    public static void showSettingsMenu(Player player, PlayerData data) {
        var locale = Find.locale(player.locale);
        showMenu(player, settingsMenu, "commands.settings.header", "commands.settings.content", new String[][] {
                {button("settings.alerts", locale, data.alerts)},
                {button("settings.effects", locale, data.effects)},
                {button("settings.doubleTapHistory", locale, data.doubleTapHistory)},
                {button("settings.welcomeMessage", locale, data.welcomeMessage)},
                {"ui.button.close"}
        });
    }

    public static void changeSettings(Player player, int option) {
        if (option == -1 || option == 4) return; // Меню закрыто

        getPlayerData(player.uuid()).subscribe(data -> {
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

    public static String button(String key, Locale locale, boolean value) {
        return format(key, locale, get(value ? "settings.on" : "settings.off", locale));
    }
}