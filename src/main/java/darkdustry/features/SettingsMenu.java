package darkdustry.features;

import darkdustry.utils.Find;
import mindustry.gen.Player;
import reactor.core.publisher.Mono;

import static darkdustry.components.Bundle.format;
import static darkdustry.components.MenuHandler.*;
import static darkdustry.components.MongoDB.*;

public class SettingsMenu {

    // TODO
    public static void showSettingsMenu(Player player) {
        var locale = Find.locale(player.locale);
        getPlayerData(player.uuid()).subscribe(data -> {
            showMenu(player, settingsMenu, "commands.settings.header", "commands.settings.content", new String[][] {
                    {format("settings.alerts", locale, data.alerts)},
                    {format("settings.effects", locale, data.effects)},
                    {format("settings.doubleTapHistory", locale, data.doubleTapHistory)},
                    {format("settings.welcomeMessage", locale, data.welcomeMessage)},
            });
        });
    }

    public static void changeSettings(Player player, int option) {
        if (option == 4) return;

        getPlayerData(player.uuid()).subscribe(data -> {
            switch (option) {
                case 0 -> data.alerts = !data.alerts;
                case 1 -> data.effects = !data.effects;
                case 2 -> data.doubleTapHistory = !data.doubleTapHistory;
                case 3 -> data.welcomeMessage = !data.welcomeMessage;
            }

            setPlayerData(data).subscribe(result -> showSettingsMenu(player));
        });
    }
}