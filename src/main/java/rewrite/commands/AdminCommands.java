package rewrite.commands;

import arc.util.CommandHandler;
import arc.util.CommandHandler.CommandRunner;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.graphics.Pal;
import mindustry.world.Block;
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

        });
        register("unit", (args, player) -> {

        });
        register("spawn", (args, player) -> {

        });
        register("tp", (args, player) -> {

        });
        register("fill", (args, player) -> { // старый добрый fill

        });
        register("full", (args, player) -> { // специально для мода, принимает 4 координаты и 3 блока

        });
    }

    @Override
    public void register(String name, CommandRunner<Player> runner) {
        super.register(name, (args, player) -> {
            if (notAdmin(player)) return;
            runner.accept(args, player);
        });
    }
}
