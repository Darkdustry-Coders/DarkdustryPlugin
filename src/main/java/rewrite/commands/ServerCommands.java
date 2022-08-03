package rewrite.commands;

import arc.func.Cons;
import arc.util.Structs;
import mindustry.game.Gamemode;
import mindustry.maps.Map;
import mindustry.maps.MapException;
import rewrite.DarkdustryPlugin;
import rewrite.utils.Find;

import static arc.Core.*;
import static mindustry.Vars.*;
import static rewrite.utils.Checks.*;

public enum ServerCommands implements Cons<String[]> {
    help("Список всех команд.", (args) -> {

    }),
    version("Информация о версии сервера.", (args) -> {

    }),
    exit("Выключить сервер.", (args) -> {
        DarkdustryPlugin.info("Выключаю сервер...");
        System.exit(2);
    }),
    stop("Остановить сервер.", (args) -> {

    }),
    host("Запустить сервер на выбранной карте.", "[карта] [режим]", (args) -> {
        if (isLanuched()) return;

        Gamemode mode = args.length > 1 ? Structs.find(Gamemode.all, m -> m.name().equalsIgnoreCase(args[1])) : Gamemode.survival;
        if (notFound(mode, args[1])) return;

        Map map;
        if (args.length > 0) {
            map = Find.map(args[0]);
            if (notFound(map, args[0])) return;
        } else {
            map = maps.getShuffleMode().next(mode, state.map);
            DarkdustryPlugin.info("Случайным образом выбрана карта: '@'.", map.name());
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

    public final String description;
    public final String params;
    private final Cons<String[]> runner;

    ServerCommands(String description, Cons<String[]> runner) {
        this(description, "", runner);
    }

    ServerCommands(String description, String params, Cons<String[]> runner) {
        this.description = description;
        this.params = params;
        this.runner = runner;
    }

    @Override
    public void get(String[] args) {
        runner.get(args);
    }
}