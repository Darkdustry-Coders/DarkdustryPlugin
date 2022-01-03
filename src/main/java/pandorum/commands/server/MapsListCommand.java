package pandorum.commands.server;

import arc.util.Log;

import static mindustry.Vars.customMapDirectory;
import static mindustry.Vars.maps;

public class MapsListCommand {
    public static void run(final String[] args) {
        if (maps.customMaps().isEmpty()) {
            Log.info("На сервере нет ни одной загруженной карты.");
        } else {
            Log.info("Карты сервера: (@)", maps.customMaps().size);
            maps.customMaps().each(map -> Log.info("  @ (@): / @x@", map.name(), map.file.name(), map.width, map.height));
        }
        Log.info("Все карты находятся здесь: &fi@", customMapDirectory.file().getAbsoluteFile().toString());
    }
}
