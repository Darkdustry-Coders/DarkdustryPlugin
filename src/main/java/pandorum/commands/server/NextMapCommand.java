package pandorum.commands.server;

import arc.ApplicationListener;
import arc.Core;
import arc.util.Log;
import arc.util.Reflect;
import mindustry.maps.Map;
import mindustry.server.ServerControl;

import static pandorum.Misc.findMap;

public class NextMapCommand {
    public static void run(final String[] args) {
        Map map = findMap(args[0]);
        if (map != null) {
            for (ApplicationListener listener : Core.app.getListeners()) {
                if (listener instanceof ServerControl control) {
                    Reflect.set(control, "nextMapOverride", map);
                }
            }
            Log.info("Следующая карта теперь '@'.", map.name());
        } else {
            Log.err("Карта '@' не найдена.", args[0]);
        }
    }
}
