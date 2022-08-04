package rewrite.commands;

import arc.util.CommandHandler;
import mindustry.game.Gamemode;
import mindustry.maps.Map;
import mindustry.maps.MapException;
import rewrite.DarkdustryPlugin;
import rewrite.utils.Find;

import java.util.Locale;

import static arc.Core.*;
import static mindustry.Vars.*;
import static rewrite.utils.Checks.*;

public class ServerCommands extends Commands<NullPointerException> {

    public ServerCommands(CommandHandler handler, Locale locale) {
        super(handler, locale);

        register("help", args -> {

        });
        register("version", args -> {

        });
        register("exit", args -> {
            DarkdustryPlugin.info("Выключаю сервер...");
            System.exit(2);
        });
        register("stop", args -> {

        });
        register("host", args -> {
            if (isLanuched()) return;

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
