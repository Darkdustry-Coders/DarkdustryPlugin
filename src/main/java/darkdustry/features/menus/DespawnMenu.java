package darkdustry.features.menus;

import arc.struct.Seq;
import mindustry.game.Team;
import mindustry.gen.*;
import useful.Bundle;

import static darkdustry.features.menus.MenuHandler.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.state;
import static useful.Bundle.bundled;

public class DespawnMenu {

    public static void showDespawnMenu(Player player) {
        showMenu(player, "commands.despawn.header", "commands.despawn.content", new String[][] {
                {"commands.despawn.button.all"},
                {"commands.despawn.button.players"},
                {Bundle.format("commands.despawn.button.team", player, coloredTeam(player.team()))},
                {Bundle.format("commands.despawn.button.team", player, coloredTeam(getEnemyTeam(player.team())))},
                {"commands.despawn.button.suicide"},
                {"ui.button.close"}}, DespawnMenu::despawnUnits
        );
    }

    public static void despawnUnits(Player player, int option) {
        switch (option) {
            case 0 -> showMenuConfirmDespawn(player, Seq.with(Groups.unit));
            case 1 -> showMenuConfirmDespawn(player, Seq.with(Groups.unit).filter(Unit::isPlayer));
            case 2 -> showMenuConfirmDespawn(player, player.team().data().units);
            case 3 -> showMenuConfirmDespawn(player, getEnemyTeam(player.team()).data().units);
            case 4 -> {
                Call.unitEnvDeath(player.unit());
                bundled(player, "commands.despawn.success.suicide");
            }
        }
    }

    public static void showMenuConfirmDespawn(Player player, Seq<Unit> units) {
        showMenuConfirm(player, "commands.despawn.header", "commands.despawn.confirm", () -> {
            units.each(Call::unitEnvDeath);
            bundled(player, "commands.despawn.success", units.size);
        }, () -> showDespawnMenu(player), units.size);
    }

    public static Team getEnemyTeam(Team team) {
        return team == state.rules.defaultTeam ? state.rules.waveTeam : state.rules.defaultTeam;
    }
}