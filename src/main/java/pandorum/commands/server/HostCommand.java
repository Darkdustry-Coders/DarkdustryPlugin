package pandorum.commands.server;

import arc.Core;
import arc.util.Log;
import arc.util.Reflect;
import mindustry.game.Gamemode;
import mindustry.maps.Map;
import mindustry.maps.MapException;
import mindustry.server.ServerControl;

import static mindustry.Vars.*;
import static pandorum.Misc.findMap;

public class HostCommand {
    public static void run(final String[] args) {
        if (!state.isMenu()) {
            Log.err("Сервер уже запущен. Используй 'stop', чтобы остановить его.");
            return;
        }

        Gamemode mode = Gamemode.survival;

        if (args.length > 0) {
            try {
                mode = Gamemode.valueOf(args[0]);
            } catch (IllegalArgumentException e) {
                Log.err("Режим игры '@' не найден.", args[0]);
                return;
            }
        }

        Map map;
        if (args.length > 1) {
            map = findMap(args[1]);
            if (map == null) {
                Log.err("Карта '@' не найдена.", args[1]);
                return;
            }
        } else {
            map = maps.getShuffleMode().next(mode, state.map);
            Log.info("Случайным образом выбрана карта: @.", map.name());
        }

        Log.info("Загружаю карту...");

        logic.reset();
        Reflect.set(ServerControl.class, "lastMode", mode);
        Core.settings.put("lastServerMode", mode.name());
        try {
            world.loadMap(map, map.applyRules(mode));
            state.rules = map.applyRules(mode);
            logic.play();

            Log.info("Карта загружена.");

            netServer.openServer();
        } catch (MapException e) {
            Log.err("@: @", e.map.name(), e.getMessage());
        }
    }
}
