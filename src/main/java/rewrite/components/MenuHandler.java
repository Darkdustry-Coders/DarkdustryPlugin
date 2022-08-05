package rewrite.components;

import arc.Events;
import mindustry.game.EventType.*;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.ui.Menus;
import rewrite.features.Ranks.Rank;
import rewrite.utils.Find;

import static mindustry.Vars.*;
import static rewrite.components.Bundle.*;
import static rewrite.components.Database.*;
import static rewrite.utils.Utils.*;

import java.util.Locale;

public class MenuHandler {

    public static int welcomeMenu, despawnMenu, artvMenu, statsMenu, rankInfoMenu, ranksRequirementsMenu, rankIncreaseMenu;

    public static void load() {
        welcomeMenu = Menus.registerMenu((player, option) -> {
            if (option != 1) return;
            PlayerData data = getPlayerData(player);
            data.welcomeMessage = false;
            bundled(player, "welcome.disabled");
            setPlayerData(data);
        });

        despawnMenu = Menus.registerMenu((player, option) -> {
            if (!player.admin) return;

            switch (option) {
                case 0 -> {
                    Groups.unit.each(Unit::kill);
                    bundled(player, "commands.despawn.success.all");
                }
                case 2 -> {
                    Groups.unit.each(Unit::isPlayer, Unit::kill);
                    bundled(player, "commands.despawn.success.players");
                }
                case 3 -> {
                    Groups.unit.each(unit -> unit.team == state.rules.defaultTeam, Unit::kill);
                    bundled(player, "commands.despawn.success.team", coloredTeam(state.rules.defaultTeam));
                }
                case 4 -> {
                    Groups.unit.each(unit -> unit.team == state.rules.waveTeam, Unit::kill);
                    bundled(player, "commands.despawn.success.team", coloredTeam(state.rules.waveTeam));
                }
                case 5 -> {
                    Call.unitCapDeath(player.unit());
                    bundled(player, "commands.despawn.success.suicide");
                }
            }
        });

        artvMenu = Menus.registerMenu((player, option) -> {
            if (!player.admin || option != 0) return;

            Events.fire(new GameOverEvent(state.rules.waveTeam));
            sendToChat("commands.artv.info", player.name);

        });

        statsMenu = emptyMenu();

        rankInfoMenu = Menus.registerMenu((player, option) -> {
            if (option != 1) return;

            StringBuilder builder = new StringBuilder();
            Rank.ranks.each(rank -> rank.req != null, rank -> builder.append(rank.localisedReq(Find.locale(player.locale))).append("\n"));
            showMenu(player, ranksRequirementsMenu, "commands.rank.menu.requirements.header", builder.toString(), new String[][] { { "ui.menus.close" } });
        });

        ranksRequirementsMenu = emptyMenu();

        rankIncreaseMenu = emptyMenu();
    }

    public static void showMenu(Player player, int menu, String title, String cont, String[][] buttons) {
        showMenu(player, menu, title, cont, buttons, null);
    }

    public static void showMenu(Player player, int menu, String title, String cont, String[][] buttons, Object titleobj, Object... contobjs) {
        Locale locale = Find.locale(player.name);
        for (int i = 0; i < buttons.length; i++)
            for (int j = 0; j < buttons[i].length; j++)
                buttons[i][j] = get(buttons[i][j], locale);
        Call.menu(player.con, menu, format(title, titleobj), format(cont, contobjs), buttons);
    }

    private static int emptyMenu() {
        return Menus.registerMenu((player, option) -> {});
    }
}
