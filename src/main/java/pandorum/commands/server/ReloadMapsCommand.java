package pandorum.commands.server;

import arc.func.Cons;
import arc.util.Log;

import static mindustry.Vars.maps;

public class ReloadMapsCommand implements Cons<String[]> {
    public void get(String[] args) {
        int mapsBefore = maps.all().size;
        maps.reload();
        int mapsAfter = maps.all().size;

        if (mapsAfter > mapsBefore) {
            Log.info("Добавлено новых карт: @.", mapsAfter - mapsBefore);
        } else if (mapsAfter < mapsBefore) {
            Log.info("Удалено старых карт: @.", mapsBefore - mapsAfter);
        } else {
            Log.info("Список карт перезагружен.");
        }
    }
}
