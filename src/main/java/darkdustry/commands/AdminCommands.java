package darkdustry.commands;

import arc.util.CommandHandler.CommandRunner;
import darkdustry.components.*;
import darkdustry.features.menus.MenuHandler;
import darkdustry.utils.Find;
import mindustry.content.Blocks;
import mindustry.gen.*;
import useful.Bundle;

import static arc.util.Strings.*;
import static darkdustry.PluginVars.*;
import static darkdustry.features.menus.MenuHandler.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;
import static useful.Bundle.*;

public class AdminCommands {

    public static void load() {
        register("a", (args, player) -> Translator.translate(Player::admin, player, args[0], "commands.a.chat", player.coloredName()));

        if (config.mode.useRtv()) {
            register("artv", (args, player) -> {
                var map = args.length > 0 ? Find.map(args[0]) : maps.getNextMap(state.rules.mode(), state.map);
                if (notFound(player, map)) return;

                showConfirmMenu(player, "commands.artv.confirm", () -> {
                    Bundle.send("commands.artv.info", player.coloredName(), map.name());
                    reloadWorld(() -> world.loadMap(map, map.applyRules(state.rules.mode())));
                }, map.name());
            });
        }

        register("despawn", (args, player) -> {
            if (args.length == 0) {
                MenuHandler.showDespawnMenu(player);
                return;
            }

            var target = Find.player(args[0]);
            if (notFound(player, target)) return;

            Call.unitEnvDeath(target.unit());

            Bundle.send(target, "despawn.success.suicide");
            if (target != player)
                Bundle.send(player, "despawn.success.player", target.coloredName());
        });

        if (!config.mode.isDefault()) return;

        register("core", (args, player) -> {
            var core = args.length > 0 ? Find.core(args[0]) : Blocks.coreShard;
            if (notFoundCore(player, core)) return;

            var team = args.length > 1 ? Find.team(args[1]) : player.team();
            if (notFound(player, team)) return;

            Call.constructFinish(player.tileOn(), core, player.unit(), (byte) 0, team, null);
            Bundle.send(player, player.blockOn() == core ? "commands.core.success" : "commands.core.failed", Icons.name(core), coloredTeam(team));
        });

        register("team", (args, player) -> {
            var team = Find.team(args[0]);
            if (notFound(player, team)) return;

            var target = args.length > 1 ? Find.player(args[1]) : player;
            if (notFound(player, target)) return;

            target.team(team);

            Bundle.send(target, "commands.team.success", coloredTeam(team));
            if (target != player)
                Bundle.send(player, "commands.team.success.player", target.coloredName(), coloredTeam(team));
        });

        register("unit", (args, player) -> {
            var type = Find.unit(args[0]);
            if (notFound(player, type)) return;

            var target = args.length > 1 ? Find.player(args[1]) : player;
            if (notFound(player, target)) return;

            var unit = type.spawn(target.team(), target.x, target.y);
            target.unit(unit);

            Bundle.send(target, "commands.unit.success", Icons.name(type));
            if (target != player)
                Bundle.send(player, "commands.unit.success.player", target.coloredName(), Icons.name(type));
        });

        register("effect", (args, player) -> {
            var effect = Find.effect(args[0]);
            if (notFound(player, effect)) return;

            int duration = args.length > 1 ? parseInt(args[1]) : 60;
            if (invalidDuration(player, duration, 0, maxEffectDuration)) return;

            var target = args.length > 2 ? Find.player(args[2]) : player;
            if (notFound(player, target)) return;

            if (duration > 0) target.unit().apply(effect, duration * 60f);
            else target.unit().unapply(effect);

            Bundle.send(target, duration > 0 ? "commands.effect.apply.success" : "commands.effect.remove.success", Icons.name(effect), duration);
            if (target != player)
                Bundle.send(player, duration > 0 ? "commands.effect.apply.success.player" : "commands.effect.remove.success.player", target.coloredName(), Icons.name(effect), duration);
        });

        register("give", (args, player) -> {
            var item = Find.item(args[0]);
            if (notFound(player, item)) return;

            int amount = args.length > 1 ? parseInt(args[1]) : 1;
            if (invalidAmount(player, amount, 1, maxGiveAmount)) return;

            var team = args.length > 2 ? Find.team(args[2]) : player.team();
            if (notFound(player, team) || noCores(player, team)) return;

            team.items().add(item, amount);
            Bundle.send(player, "commands.give.success", amount, Icons.name(item), coloredTeam(team));
        });

        register("spawn", (args, player) -> {
            var type = Find.unit(args[0]);
            if (notFound(player, type)) return;

            int amount = args.length > 1 ? parseInt(args[1]) : 1;
            if (invalidAmount(player, amount, 1, maxSpawnAmount)) return;

            var team = args.length > 2 ? Find.team(args[2]) : player.team();
            if (notFound(player, team)) return;

            for (int i = 0; i < amount; i++) type.spawn(team, player);
            Bundle.send(player, "commands.spawn.success", amount, Icons.name(type), coloredTeam(team));
        });

        register("tp", (args, player) -> {
            int x = parseInt(args[0]), y = parseInt(args[1]);
            if (invalidCoordinates(player, x, y)) return;

            var unit = player.unit();
            player.clearUnit();

            unit.set(x * tilesize, y * tilesize);
            Call.setPosition(player.con, x * tilesize, y * tilesize);
            Call.setCameraPosition(player.con, x * tilesize, y * tilesize);

            player.unit(unit);

            Bundle.send(player, "commands.tp.success", x, y);
        });

        register("fill", (args, player) -> {
            var block = Find.block(args[0]);
            if (notFound(player, block)) return;

            int width = parseInt(args[1]), height = parseInt(args[2]);
            if (invalidArea(player, width, height, maxFillArea)) return;

            for (int x = player.tileX(); x < player.tileX() + width; x += block.size)
                for (int y = player.tileY(); y < player.tileY() + height; y += block.size) {
                    var tile = world.tile(x, y);
                    if (tile == null) continue;

                    if (block.isAir()) tile.removeNet();
                    else if (block.isOverlay()) tile.setFloorNet(tile.floor(), block);
                    else if (block.isFloor()) tile.setFloorNet(block, tile.overlay());
                    else tile.setNet(block, player.team(), 0);
                }

            Bundle.send(player, "commands.fill.success", width, height, Icons.name(block));
        });
    }

    public static void register(String name, CommandRunner<Player> runner) {
        adminOnlyCommands.add(clientCommands.<Player>register(name, Bundle.get("commands." + name + ".params", "", defaultLocale), Bundle.get("commands." + name + ".description", defaultLocale), (args, player) -> {
            if (notAdmin(player)) return;
            runner.accept(args, player);
        }).text);
    }
}