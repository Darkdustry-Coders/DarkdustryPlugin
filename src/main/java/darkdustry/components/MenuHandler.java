package darkdustry.components;

import arc.struct.Seq;
import darkdustry.features.Ranks;
import mindustry.gen.*;
import mindustry.ui.Menus.MenuListener;
import useful.*;

import static darkdustry.PluginVars.discordServerUrl;
import static darkdustry.components.MongoDB.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.state;
import static useful.Bundle.bundled;

public class MenuHandler {

    public static MenuListener empty = (player, option) -> {};

    public static MenuListener confirmed(Runnable confirmed) {
        return (player, option) -> {
            if (option == 0) confirmed.run();
        };
    }

    public static void welcomeMenu(Player player, int option) {
        switch (option) {
            case 1 -> Call.openURI(player.con, discordServerUrl);
            case 2 -> getPlayerData(player.uuid()).subscribe(data -> {
                data.welcomeMessage = false;
                setPlayerData(data).subscribe();
                bundled(player, "welcome.disabled");
            });
        }
    }

    public static void despawnMenu(Player player, int option) {
        var units = Seq.with(Groups.unit);

        // TODO
        switch (option) {
            case 0 -> {
                units = Groups.unit;
            }
            case 1 -> units = mapPlayers(Player::unit);
        }

        showMenuConfirm(player, () -> {
            units.each(Unit::kill);
        }, "", "", units.size);
    }

    public static void rankInfo(Player player, int option) {
        if (option != 1) return;

        var builder = new StringBuilder();
        Ranks.all.each(rank -> rank.req != null, rank -> builder.append(rank.localisedReq(player)).append("\n"));

        showMenuClose(player, "commands.rank.requirements.header", builder.toString());
    }

    public static void showMenu(Player player, MenuListener listener, String title, String content, String[][] buttons, Object... values) {
        for (int i = 0; i < buttons.length; i++)
            for (int j = 0; j < buttons[i].length; j++)
                buttons[i][j] = Bundle.get(buttons[i][j], player);

        DynamicMenus.menu(player, Bundle.get(title, player), Bundle.format(content, player, values), buttons, listener);
    }

    public static void showMenuClose(Player player, String title, String content, Object... values) {
        showMenu(player, empty, title, content, new String[][] {{"ui.button.close"}}, values);
    }

    public static void showMenuConfirm(Player player, Runnable confirmed, String title, String content, Object... values) {
        showMenu(player, confirmed(confirmed), title, content, new String[][] {{"ui.button.yes", "ui.button.no"}}, values);
    }
}