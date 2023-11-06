package darkdustry.commands;

import arc.util.Strings;
import darkdustry.features.menus.MenuHandler;
import darkdustry.features.net.Translator;
import darkdustry.utils.Find;
import mindustry.gen.Call;
import mindustry.gen.Player;
import useful.Bundle;
import useful.Commands;

import static darkdustry.PluginVars.*;
import static darkdustry.config.Config.config;
import static darkdustry.utils.Checks.*;
import static mindustry.Vars.*;
import static mindustry.server.ServerControl.instance;

public class AdminCommands {

    public static void load() {
        Commands.admin("a")
                .register((args, player) -> Translator.translate(Player::admin, player, args[0], "commands.a.chat", player.coloredName()));

        Commands.admin("artv")
                .enabled(config.mode.enableRtv)
                .register((args, player) -> {
                    var map = args.length > 0 ? Find.map(args[0]) : maps.getNextMap(instance.lastMode, state.map);
                    if (notFound(player, map)) return;

                    MenuHandler.showConfirmMenu(player, "commands.artv.confirm", () -> {
                        Bundle.send("commands.artv.info", player.coloredName(), map.name());
                        instance.play(false, () -> world.loadMap(map));
                    }, map.name());
                });

        Commands.admin("despawn")
                .register((args, player) -> {
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

        Commands.admin("core")
                .enabled(config.mode.isDefault)
                .register((args, player) -> {
                    var core = args.length > 0 ? Find.core(args[0]) : state.rules.planet.defaultCore;
                    if (notFoundCore(player, core)) return;

                    var team = args.length > 1 ? Find.team(args[1]) : player.team();
                    if (notFound(player, team)) return;

                    Call.constructFinish(player.tileOn(), core, player.unit(), (byte) 0, team, null);
                    Bundle.send(player, player.blockOn() == core ? "commands.core.success" : "commands.core.failed", core.emoji(), core.name, team.coloredName());
                });

        Commands.admin("team")
                .enabled(config.mode.isDefault)
                .register((args, player) -> {
                    var team = Find.team(args[0]);
                    if (notFound(player, team)) return;

                    var target = args.length > 1 ? Find.player(args[1]) : player;
                    if (notFound(player, target)) return;

                    target.team(team);

                    Bundle.send(target, "commands.team.success", team.coloredName());
                    if (target != player)
                        Bundle.send(player, "commands.team.success.player", target.coloredName(), team.coloredName());
                });

        Commands.admin("unit")
                .enabled(config.mode.isDefault)
                .register((args, player) -> {
                    var type = Find.unit(args[0]);
                    if (notFound(player, type)) return;

                    var target = args.length > 1 ? Find.player(args[1]) : player;
                    if (notFound(player, target)) return;

                    var unit = type.spawn(target.team(), target.x, target.y);
                    target.unit(unit);

                    Bundle.send(target, "commands.unit.success", type.emoji(), type.name);
                    if (target != player)
                        Bundle.send(player, "commands.unit.success.player", target.coloredName(), type.emoji(), type.name);
                });

        Commands.admin("effect")
                .enabled(config.mode.isDefault)
                .register((args, player) -> {
                    var effect = Find.effect(args[0]);
                    if (notFound(player, effect)) return;

                    int duration = args.length > 1 ? Strings.parseInt(args[1]) : 60;
                    if (invalidDuration(player, duration, 0, maxEffectDuration)) return;

                    var target = args.length > 2 ? Find.player(args[2]) : player;
                    if (notFound(player, target)) return;

                    if (duration > 0) target.unit().apply(effect, duration * 60f);
                    else target.unit().unapply(effect);

                    Bundle.send(target, duration > 0 ? "commands.effect.apply.success" : "commands.effect.remove.success", effect.emoji(), effect.name, duration);
                    if (target != player)
                        Bundle.send(player, duration > 0 ? "commands.effect.apply.success.player" : "commands.effect.remove.success.player", target.coloredName(), effect.emoji(), effect.name, duration);
                });

        Commands.admin("give")
                .enabled(config.mode.isDefault)
                .register((args, player) -> {
                    var item = Find.item(args[0]);
                    if (notFound(player, item)) return;

                    int amount = args.length > 1 ? Strings.parseInt(args[1]) : 1;
                    if (invalidAmount(player, amount, 1, maxGiveAmount)) return;

                    var team = args.length > 2 ? Find.team(args[2]) : player.team();
                    if (notFound(player, team) || noCores(player, team)) return;

                    team.items().add(item, amount);
                    Bundle.send(player, "commands.give.success", amount, item.emoji(), item.name, team.coloredName());
                });

        Commands.admin("spawn")
                .enabled(config.mode.isDefault)
                .register((args, player) -> {
                    var type = Find.unit(args[0]);
                    if (notFound(player, type)) return;

                    int amount = args.length > 1 ? Strings.parseInt(args[1]) : 1;
                    if (invalidAmount(player, amount, 1, maxSpawnAmount)) return;

                    var team = args.length > 2 ? Find.team(args[2]) : player.team();
                    if (notFound(player, team)) return;

                    for (int i = 0; i < amount; i++) type.spawn(team, player);
                    Bundle.send(player, "commands.spawn.success", amount, type.emoji(), type.name, team.coloredName());
                });

        Commands.admin("tp")
                .enabled(config.mode.isDefault)
                .register((args, player) -> {
                    int x = Strings.parseInt(args[0]), y = Strings.parseInt(args[1]);
                    if (invalidCoordinates(player, x, y)) return;

                    var unit = player.unit();
                    player.clearUnit();

                    unit.set(x * tilesize, y * tilesize);
                    Call.setPosition(player.con, x * tilesize, y * tilesize);
                    Call.setCameraPosition(player.con, x * tilesize, y * tilesize);

                    player.unit(unit);
                    Bundle.send(player, "commands.tp.success", x, y);
                });

        Commands.admin("fill")
                .enabled(config.mode.isDefault)
                .register((args, player) -> {
                    var block = Find.block(args[0]);
                    if (notFound(player, block)) return;

                    int width = Strings.parseInt(args[1]), height = Strings.parseInt(args[2]);
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

                    Bundle.send(player, "commands.fill.success", width, height, block.emoji(), block.name);
                });
    }
}