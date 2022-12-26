package darkdustry.features.menus;

import arc.struct.Seq;
import mindustry.gen.*;
import useful.Bundle;

import static darkdustry.features.menus.MenuHandler.*;
import static darkdustry.utils.Utils.coloredTeam;
import static mindustry.Vars.state;
import static useful.Bundle.bundled;

public class DespawnMenu {

    public static void show(Player player) {
        showMenu(player, "commands.despawn.header", "commands.despawn.content", new String[][] {
                {"commands.despawn.button.all"},
                {"commands.despawn.button.not-players"},
                {Bundle.format("commands.despawn.button.team", player, coloredTeam(state.rules.defaultTeam))},
                {Bundle.format("commands.despawn.button.team", player, coloredTeam(state.rules.waveTeam))},
                {"commands.despawn.button.suicide"},
                {"ui.button.close"}}, option -> selection(player, option)
        );
    }

    public static void selection(Player player, int option) {
        if (!player.admin) return;

        switch (option) {
            case 0 -> showMenuConfirmDespawn(player, Groups.unit.copy(new Seq<>()));
            case 1 -> showMenuConfirmDespawn(player, Groups.unit.copy(new Seq<>()).removeAll(Unit::isPlayer));
            case 2 -> showMenuConfirmDespawn(player, state.rules.defaultTeam.data().units);
            case 3 -> showMenuConfirmDespawn(player, state.rules.waveTeam.data().units);
            case 4 -> {
                Call.unitEnvDeath(player.unit());
                bundled(player, "commands.despawn.success.suicide");
            }
        }
    }

    public static void showMenuConfirmDespawn(Player player, Seq<Unit> units) {
        showMenuConfirm(player, "commands.despawn.confirm", () -> {
            units.each(Call::unitEnvDeath);
            bundled(player, "commands.despawn.success", units.size);
        }, () -> show(player), units.size);
    }
}