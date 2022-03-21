package pandorum.commands.server;

import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.maps.Map;

import static mindustry.Vars.customMapDirectory;
import static mindustry.Vars.maps;

public class MapsListCommand implements Cons<String[]> {
    public void get(String[] args) {
        Seq<Map> mapsList = maps.customMaps();
        if (mapsList.isEmpty()) {
            Log.info("На сервере нет ни одной загруженной карты.");
        } else {
            Log.info("Карты сервера: (@)", mapsList.size);
            mapsList.each(map -> Log.info("  '@' (@): / @x@", map.name(), map.file.name(), map.width, map.height));
        }
        Log.info("Все карты находятся здесь: &fi@", customMapDirectory.absolutePath());
    }
}
