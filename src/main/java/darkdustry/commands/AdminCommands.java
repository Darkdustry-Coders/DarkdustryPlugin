package darkdustry.commands;

import arc.math.Mathf;
import arc.util.Strings;
import arc.util.CommandHandler.CommandRunner;
import mindustry.content.Blocks;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.graphics.Pal;
import mindustry.world.Tile;
import darkdustry.components.Icons;
import darkdustry.utils.Find;

import static mindustry.Vars.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.*;
import static darkdustry.components.MenuHandler.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;

public class AdminCommands {

    public static void load() {
        register("a", (args, player) -> Groups.player.each(Player::admin, admin -> bundled(admin, "commands.a.chat", Pal.adminChat, player.coloredName(), args[0])));

        register("artv", (args, player) -> showMenu(player, artvMenu, "commands.artv.menu.header", "commands.artv.menu.content",
                new String[][] {{"ui.menus.yes", "ui.menus.no"}}));

        register("despawn", (args, player) -> {
            if (args.length > 0) {
                var target = Find.player(args[0]);
                if (notFound(player, target)) return;

                Call.unitEnvDeath(target.unit());
                bundled(target, "commands.despawn.success.suicide");
                if (target != player) bundled(player, "commands.despawn.success.player", target.coloredName());
            } else
                showMenu(player, despawnMenu, "commands.despawn.menu.header", "commands.despawn.menu.content", new String[][] {
                        {"ui.menus.yes", "ui.menus.no"}, {"commands.despawn.menu.players"},
                        {format("commands.despawn.menu.team", Find.locale(player.locale), coloredTeam(state.rules.defaultTeam))},
                        {format("commands.despawn.menu.team", Find.locale(player.locale), coloredTeam(state.rules.waveTeam))},
                        {"commands.despawn.menu.suicide"}}, null, Groups.unit.size());
        });

        register("team", (args, player) -> {
            var team = Find.team(args[0]);
            if (notFound(player, team)) return;

            var target = args.length > 1 ? Find.player(args[1]) : player;
            if (notFound(player, target)) return;

            target.team(team);
            bundled(target, "commands.team.success", coloredTeam(team));
            if (target != player)
                bundled(player, "commands.team.success.player", target.coloredName(), coloredTeam(team));
        });

        register("core", (args, player) -> {
            var core = args.length > 0 ? Find.core(args[0]) : Blocks.coreShard;
            if (notFoundCore(player, core)) return;

            var team = args.length > 1 ? Find.team(args[1]) : player.team();
            if (notFound(player, team)) return;

            Call.constructFinish(player.tileOn(), core, player.unit(), (byte) 0, team, false);
            bundled(player, player.blockOn() == core ? "commands.core.success" : "commands.core.failed", Icons.get(core.name), coloredTeam(team));
        });

        register("give", (args, player) -> {
            if (invalidAmount(player, args, 1)) return;

            var item = Find.item(args[0]);
            if (notFound(player, item)) return;

            int amount = args.length > 1 ? Strings.parseInt(args[1]) : 1;
            if (invalidGiveAmount(player, amount)) return;

            var team = args.length > 2 ? Find.team(args[2]) : player.team();
            if (notFound(player, team) || notFoundCore(player, team)) return;

            team.core().items.add(item, amount);
            bundled(player, "commands.give.success", amount, Icons.get(item.name), coloredTeam(team));
        });

        register("unit", (args, player) -> {
            var type = Find.unit(args[0]);
            if (notFound(player, type)) return;

            var target = args.length > 1 ? Find.player(args[1]) : player;
            if (notFound(player, target)) return;

            target.unit(type.spawn(target.team(), target.x, target.y));
            target.unit().spawnedByCore(true);
            bundled(target, "commands.unit.success", Icons.get(type.name));
            if (target != player)
                bundled(player, "commands.unit.success.player", target.coloredName(), Icons.get(type.name));
        });

        register("spawn", (args, player) -> {
            if (invalidAmount(player, args, 1)) return;

            var type = Find.unit(args[0]);
            if (notFound(player, type)) return;

            int amount = args.length > 1 ? Strings.parseInt(args[1]) : 1;
            if (invalideSpawnAmount(player, amount)) return;

            var team = args.length > 2 ? Find.team(args[2]) : player.team();
            if (notFound(player, team)) return;

            for (int i = 0; i < amount; i++) type.spawn(team, player);
            bundled(player, "commands.spawn.success", amount, Icons.get(type.name), coloredTeam(team));
        });

        register("tp", (args, player) -> {
            if (invalidTpCoords(player, args)) return;
            int x = Mathf.clamp(Strings.parseInt(args[0]), 0, world.width()), y = Mathf.clamp(Strings.parseInt(args[1]), 0, world.height());

            boolean spawnedNyCore = player.unit().spawnedByCore();
            var unit = player.unit();

            unit.spawnedByCore(false);
            player.clearUnit();

            unit.set(x * tilesize, y * tilesize);
            Call.setPosition(player.con, x * tilesize, y * tilesize);
            Call.setCameraPosition(player.con, x * tilesize, y * tilesize);

            player.unit(unit);
            unit.spawnedByCore(spawnedNyCore);
            bundled(player, "commands.tp.success", x, y);
        });

        register("fill", (args, player) -> {
            if (invalidFillAmount(player, args)) return;
            int width = Strings.parseInt(args[0]), height = Strings.parseInt(args[1]);
            if (invalidFillAmount(player, width, height)) return;

            var block = Find.block(args[2]);
            if (notFound(player, block)) return;

            for (int x = player.tileX(); x < player.tileX() + width; x += block.size) {
                for (int y = player.tileY(); y < player.tileY() + height; y += block.size) {
                    Tile tile = world.tile(x, y);
                    if (tile == null) continue;

                    if (block.isFloor() && !block.isOverlay()) tile.setFloorNet(block, tile.overlay());
                    else if (block.isOverlay()) tile.setFloorNet(tile.floor(), block);
                    else tile.setNet(block, player.team(), 0);
                }
            }

            bundled(player, "commands.fill.success", width, height, Icons.get(block.name, block.name));
        });
    }

    public static void register(String name, CommandRunner<Player> runner) {
        clientCommands.<Player>register(name, get("commands." + name + ".params", ""), get("commands." + name + ".description", ""), (args, player) -> {
            if (player.admin) runner.accept(args, player);
            else bundled(player, "commands.permission-denied");
        });
    }
}
