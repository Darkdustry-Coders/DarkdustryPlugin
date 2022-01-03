package pandorum.commands.server;

import arc.util.Log;
import arc.util.Reflect;
import mindustry.maps.Map;
import mindustry.server.ServerControl;

import static pandorum.Misc.findMap;

public class NextMapCommand {
    public static void run(final String[] args) {
        Map map = findMap(args[0]);
        if (map != null) {
            Reflect.set(ServerControl.class, "nextMapOverride", map);
            Log.info("Следующая карта теперь '@'.", map.name());
        } else {
            Log.err("Карта '@' не найдена.", args[0]);
        }
    }
}
