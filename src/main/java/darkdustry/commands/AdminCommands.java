package darkdustry.commands;

import arc.math.Mathf;
import arc.util.CommandHandler.CommandRunner;
import darkdustry.components.Icons;
import darkdustry.features.menus.DespawnMenu;
import darkdustry.utils.Find;
import mindustry.gen.*;
import useful.Bundle;

import static arc.util.Strings.parseInt;
import static darkdustry.PluginVars.*;
import static darkdustry.features.menus.MenuHandler.showMenuConfirm;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;
import static mindustry.content.Blocks.coreShard;
import static mindustry.core.World.conv;
import static mindustry.graphics.Pal.adminChat;
import static useful.Bundle.*;

public class AdminCommands {

    public static void load() {
        register("a", (args, player) -> Groups.player.each(Player::admin, p -> bundled(p, player, args[0], "commands.a.chat", adminChat, player.coloredName(), args[0])));

        if (config.mode.useRtv()) {
            register("artv", (args, player) -> {
                var map = args.length > 0 ? Find.map(args[0]) : maps.getNextMap(state.rules.mode(), state.map);
                if (notFound(player, map)) return;

                showMenuConfirm(player, "commands.artv.confirm", () -> {
                    if (!player.admin) return;

                    sendToChat("commands.artv.info", player.coloredName(), map.name());
                    reloadWorld(() -> world.loadMap(map, map.applyRules(state.rules.mode())));
                }, map.name());
            });
        }

        register("despawn", (args, player) -> {
            if (args.length == 0) {
                DespawnMenu.show(player);
                return;
            }

            var target = Find.player(args[0]);
            if (notFound(player, target)) return;

            Call.unitEnvDeath(target.unit());
            bundled(target, "commands.despawn.success.suicide");
            if (target != player) bundled(player, "commands.despawn.success.player", target.coloredName());
        });

        if (!config.mode.isDefault()) return;

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
            var core = args.length > 0 ? Find.core(args[0]) : coreShard;
            if (notFoundCore(player, core)) return;

            var team = args.length > 1 ? Find.team(args[1]) : player.team();
            if (notFound(player, team)) return;

            Call.constructFinish(player.tileOn(), core, player.unit(), (byte) 0, team, false);
            bundled(player, player.blockOn() == core ? "commands.core.success" : "commands.core.failed", Icons.get(core), coloredTeam(team));
        });

        register("give", (args, player) -> {
            if (invalidAmount(player, args, 1)) return;

            var item = Find.item(args[0]);
            if (notFound(player, item)) return;

            int amount = args.length > 1 ? parseInt(args[1]) : 1;
            if (invalidGiveAmount(player, amount)) return;

            var team = args.length > 2 ? Find.team(args[2]) : player.team();
            if (notFound(player, team) || noCores(player, team)) return;

            team.items().add(item, amount);
            bundled(player, "commands.give.success", amount, Icons.get(item), coloredTeam(team));
        });

        register("unit", (args, player) -> {
            var type = Find.unit(args[0]);
            if (notFound(player, type)) return;

            var target = args.length > 1 ? Find.player(args[1]) : player;
            if (notFound(player, target)) return;

            var unit = type.spawn(target.team(), target.x, target.y);
            Call.unitControl(target, unit);

            bundled(target, "commands.unit.success", Icons.get(type));
            if (target != player)
                bundled(player, "commands.unit.success.player", target.coloredName(), Icons.get(type));
        });

        register("spawn", (args, player) -> {
            if (invalidAmount(player, args, 1)) return;

            var type = Find.unit(args[0]);
            if (notFound(player, type)) return;

            int amount = args.length > 1 ? parseInt(args[1]) : 1;
            if (invalidSpawnAmount(player, amount)) return;

            var team = args.length > 2 ? Find.team(args[2]) : player.team();
            if (notFound(player, team)) return;

            for (int i = 0; i < amount; i++) type.spawn(team, player);
            bundled(player, "commands.spawn.success", amount, Icons.get(type), coloredTeam(team));
        });

        register("tp", (args, player) -> {
            if (invalidTpCoords(player, args)) return;
            float
                    x = Mathf.clamp(parseInt(args[0]), 0, world.width()) * tilesize,
                    y = Mathf.clamp(parseInt(args[1]), 0, world.height()) * tilesize;

            var unit = player.unit();
            Call.unitControl(player, null);

            unit.set(x, y);
            Call.setPosition(player.con, x, y);
            Call.setCameraPosition(player.con, x, y);

            Call.unitControl(player, unit);

            bundled(player, "commands.tp.success", conv(x), conv(y));
        });

        register("fill", (args, player) -> {
            if (invalidFillAmount(player, args)) return;
            int width = parseInt(args[1]), height = parseInt(args[2]);
            if (invalidFillAmount(player, width, height)) return;

            var block = Find.block(args[0]);
            if (notFound(player, block)) return;

            for (int x = player.tileX(); x < player.tileX() + width; x += block.size)
                for (int y = player.tileY(); y < player.tileY() + height; y += block.size) {
                    var tile = world.tile(x, y);
                    if (tile == null) continue;

                    if (block.isAir()) tile.removeNet();
                    else if (block.isOverlay()) tile.setFloorNet(tile.floor(), block);
                    else if (block.isFloor()) tile.setFloorNet(block, tile.overlay());
                    else tile.setNet(block, player.team(), 0);
                }

            bundled(player, "commands.fill.success", width, height, Icons.get(block));
        });
    }

    public static void register(String name, CommandRunner<Player> runner) {
        adminOnlyCommands.add(clientCommands.<Player>register(name, Bundle.get("commands." + name + ".params", "", defaultLocale), Bundle.get("commands." + name + ".description", defaultLocale), (args, player) -> {
            if (notAdmin(player)) return;
            runner.accept(args, player);
        }).text);
    }
}