package pandorum.commands.server;

import arc.util.Log;

import static mindustry.Vars.maps;

public class ReloadMapsCommand {
    public static void run(final String[] args) {
        int beforeMaps = maps.all().size;
        maps.reload();
        if (maps.all().size > beforeMaps) {
            Log.info("@ новых карт загружено на сервер.", maps.all().size - beforeMaps);
        } else if (maps.all().size < beforeMaps) {
            Log.info("@ старых карт удалено.", beforeMaps - maps.all().size);
        } else {
            Log.info("Карты перезагружены.");
        }
    }
}
