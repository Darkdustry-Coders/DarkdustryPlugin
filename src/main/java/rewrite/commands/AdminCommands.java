package rewrite.commands;

import arc.math.Mathf;
import arc.util.CommandHandler;
import arc.util.Strings;
import arc.util.CommandHandler.CommandRunner;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import rewrite.components.Icons;
import rewrite.utils.Find;

import java.util.Locale;

import static mindustry.Vars.*;
import static rewrite.PluginVars.*;
import static rewrite.components.Bundle.*;
import static rewrite.components.MenuHandler.*;
import static rewrite.utils.Checks.*;
import static rewrite.utils.Utils.*;

public class AdminCommands extends Commands<Player> {

    public AdminCommands(CommandHandler handler, Locale def) {
        super(handler, def);

        register("a", (args, player) -> Groups.player.each(Player::admin, admin -> bundled(admin, "commands.a.chat", Pal.adminChat, player.name, args[0])));

        if (!config.mode.isDefault()) return;

        register("artv", (args, player) -> showMenu(player, artvMenu, "commands.artv.menu.header", "commands.artv.menu.content",
                new String[][] {{"ui.menus.yes", "ui.menus.no"}}));

        register("despawn", (args, player) -> {
            if (args.length > 0) {
                Player target = Find.player(args[0]);
                if (notFound(player, target, args[0])) return;

                Call.unitCapDeath(target.unit());
                bundled(target, "commands.despawn.success.suicide");
                if (target != player) bundled(player, "commands.despawn.success.player");
            } else showMenu(player, despawnMenu, "commands.despawn.menu.header", "commands.despawn.menu.content", new String[][] {
                        {"ui.menus.yes", "ui.menus.no"}, {"commands.despawn.menu.players"},
                        {format("commands.despawn.menu.team", Find.locale(player.locale), coloredTeam(state.rules.defaultTeam))},
                        {format("commands.despawn.menu.team", Find.locale(player.locale), coloredTeam(state.rules.waveTeam))},
                        {"commands.despawn.menu.suicide"}}, null, Groups.unit.size());
        });

        register("team", (args, player) -> {
            Team team = Find.team(args[0]);
            if (notFound(player, team)) return;

            Player target = args.length > 1 ? Find.player(args[1]) : player;
            if (notFound(player, target, args[1])) return;

            target.team(team);
            bundled(target, "commands.team.success", coloredTeam(team));
            if (target != player) bundled(player, "commands.team.success.player", target.name, coloredTeam(team));
        });

        register("core", (args, player) -> {
            Block core = args.length > 0 ? Find.core(args[0].toLowerCase()) : Blocks.coreShard;
            if (notFoundCore(player, core)) return;

            Team team = args.length > 1 ? Find.team(args[1]) : player.team();
            if (notFound(player, team)) return;

            Call.constructFinish(player.tileOn(), core, player.unit(), (byte) 0, team, false);
            bundled(player, player.tileOn() != null && player.tileOn().block() == core ? "commands.core.success" : "commands.core.failed",
                    Icons.get(core.name), coloredTeam(team));
        });

        register("give", (args, player) -> {
            if (invalideAmount(player, args)) return;

            Item item = Find.item(args[0]);
            if (notFound(player, item)) return;

            int amount = args.length > 1 ? Strings.parseInt(args[1]) : 1;
            if (invalideGiveAmount(player, amount)) return;

            Team team = args.length > 2 ? Find.team(args[2]) : player.team();
            if (notFound(player, team) || notFoundCore(player, team)) return;

            team.core().items.add(item, amount);
            bundled(player, "commands.give.success", amount, Icons.get(item.name), coloredTeam(team));
        });

        register("unit", (args, player) -> {
            UnitType type = Find.unit(args[0]);
            if (notFound(player, type)) return;

            Player target = args.length > 1 ? Find.player(args[1]) : player;
            if (notFound(player, target, args[0])) return;

            target.unit().spawnedByCore(true);
            target.unit(type.spawn(target.team(), target.x, target.y));
            bundled(target, "commands.unit.success", Icons.get(type.name));
            if (target != player) bundled(player, "commands.unit.success.player", target.name, Icons.get(type.name));
        });

        register("spawn", (args, player) -> {
            if (invalideAmount(player, args)) return;

            UnitType type = Find.unit(args[0]);
            if (notFound(player, type)) return;

            int amount = args.length > 1 ? Strings.parseInt(args[1]) : 1;
            if (invalideSpawnAmount(player, amount)) return;

            Team team = args.length > 2 ? Find.team(args[2]) : player.team();
            if (notFound(player, team)) return;

            for (int i = 0; i < amount; i++) type.spawn(team, player);
            bundled(player, "commands.spawn.success", amount, Icons.get(type.name), coloredTeam(team));
        });

        register("tp", (args, player) -> {
            if (invalideTpAmount(player, args)) return;
            int x = Mathf.clamp(Strings.parseInt(args[0]), 0, world.width()), y = Mathf.clamp(Strings.parseInt(args[1]), 0, world.height());

            boolean spawnedNyCore = player.unit().spawnedByCore();
            Unit unit = player.unit();

            unit.spawnedByCore(false);
            player.clearUnit();

            unit.set(x * tilesize, y * tilesize);
            Call.setPosition(player.con, x * tilesize, y * tilesize);
            Call.setCameraPosition(player.con, x * tilesize, y * tilesize);

            player.unit(unit);
            unit.spawnedByCore(spawnedNyCore);
        });

        register("fill", (args, player) -> {
            if (invalideFillAmount(player, args)) return;
            int width = Strings.parseInt(args[1]), height = Strings.parseInt(args[2]);
            if (invalideFillAmount(player, width, height)) return;

            Block block = Find.block(args[0]);
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

        register("full", (args, player) -> {
            if (invalideFullAmount(player, args)) return;
            int x1 = Strings.parseInt(args[3]), y1 = Strings.parseInt(args[4]),
                x2 = Strings.parseInt(args[5]), y2 = Strings.parseInt(args[6]),
                width = Math.abs(x1 - x2) + 1, height = Math.abs(y1 - y2) + 1;
            if (invalideFillAmount(player, width, height)) return;

            x1 = Math.min(x1, x2);
            y1 = Math.min(y1, y2);

            Block floor = Find.block(args[0]), block = Find.block(args[1]), overlay = Find.block(args[2]);
            for (int x = x1; x < x1 + width; x++) {
                for (int y = y1; y < y1 + height; y++) {
                    Tile tile = world.tile(x, y);
                    if (tile == null) continue;

                    tile.setFloorNet(floor == null ? tile.floor() : floor, overlay == null ? tile.overlay() : overlay);
                    if (block != null) tile.setNet(block);
                }
            }

            bundled(player, "commands.full.success", width, height);
        });
    }

    @Override
    public void register(String name, CommandRunner<Player> runner) {
        super.register(name, (args, player) -> {
            if (player.admin) runner.accept(args, player);
            else bundled(player, "commands.permission-denied");
        });
    }
}