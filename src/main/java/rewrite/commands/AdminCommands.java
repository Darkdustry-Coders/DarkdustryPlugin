package rewrite.commands;

import arc.util.CommandHandler;
import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;

import java.util.Locale;

import static rewrite.PluginVars.*;
import static rewrite.utils.Checks.*;

public class AdminCommands extends Commands<Player> {

    public AdminCommands(CommandHandler handler, Locale def) {
        super(handler, def);

        register("a", (args, player) -> {

        });

        if (!config.mode.isDefault()) return;

        register("artv", (args, player) -> {

        });
        register("despawn", (args, player) -> {

        });
        register("team", (args, player) -> {

        });
        register("spectate", (args, player) -> {

        });
        register("core", (args, player) -> {

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
