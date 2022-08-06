package rewrite.commands;

import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.CommandHandler.Command;
import arc.util.Log;
import arc.util.OS;
import mindustry.core.GameState.State;
import mindustry.core.Version;
import mindustry.game.Gamemode;
import mindustry.maps.Map;
import mindustry.maps.MapException;
import rewrite.DarkdustryPlugin;
import rewrite.utils.Find;

import java.util.Locale;

import static arc.Core.app;
import static mindustry.Vars.*;
import static rewrite.PluginVars.serverCommands;
import static rewrite.utils.Checks.isLaunched;
import static rewrite.utils.Checks.notFound;

public class ServerCommands extends Commands<NullPointerException> {

    public ServerCommands(CommandHandler handler, Locale def) {
        super(handler, def);

        for (String command : new String[] {"mod", "mods", "fillitems", "pause", "shuffle", "runwave"})
            handler.removeCommand(command);

        register("help", args -> {
            Seq<Command> commandsList = serverCommands.getCommandList();
            DarkdustryPlugin.info("Команды для консоли: (@)", commandsList.size);
            commandsList.each(command -> Log.info("  &b&lb" + command.text
                    + (command.paramText.isEmpty() ? "" : " &lc&fi")
                    + command.paramText + "&fr - &lw"
                    + command.description));
        });
        register("version", args -> {
            DarkdustryPlugin.info("Mindustry @-@ @ / билд @.@",
                    Version.number, Version.modifier, Version.type,
                    Version.build, Version.revision);
            DarkdustryPlugin.info("Версия Java: @", OS.javaVersion);
        });
        register("exit", args -> {
            DarkdustryPlugin.info("Выключаю сервер...");
            System.exit(2);
        });
        register("stop", args -> {
            net.closeServer();
            state.set(State.menu);
            DarkdustryPlugin.info("Сервер остановлен.");
        });
        register("host", args -> {
            if (isLaunched()) return;

            Gamemode mode;
            if (args.length > 1) {
                mode = Find.mode(args[1]);
                if (notFound(mode, args)) return;
            } else {
                mode = Gamemode.survival;
                DarkdustryPlugin.info("Выбран режим по умолчанию: @.", mode.name());
            }

            Map map;
            if (args.length > 0) {
                map = Find.map(args[0]);
                if (notFound(map, args)) return;
            } else {
                map = maps.getShuffleMode().next(mode, state.map);
                DarkdustryPlugin.info("Случайным образом выбрана карта: @.", map.name());
            }

            logic.reset();

            app.post(() -> {
                try {
                    DarkdustryPlugin.info("Загружаю карту...");

                    world.loadMap(map, map.applyRules(mode));
                    state.rules = map.applyRules(mode);
                    logic.play();
                    netServer.openServer();

                    DarkdustryPlugin.info("Карта загружена.");
                } catch (MapException exception) {
                    DarkdustryPlugin.error("@: @", exception.map.name(), exception.getMessage());
                }
            });
        });
    }
}
