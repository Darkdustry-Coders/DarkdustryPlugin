package pandorum.commands.server;

import arc.func.Cons;
import arc.util.Log;
import arc.util.Reflect;
import mindustry.maps.Map;

import static pandorum.util.Search.findMap;
import static pandorum.util.Utils.getServerControl;

public class NextMapCommand implements Cons<String[]> {
    public void get(String[] args) {
        Map map = findMap(args[0]);
        if (map != null) {
            Reflect.set(getServerControl(), "nextMapOverride", map);
            Log.info("Следующая карта теперь '@'.", map.name());
        } else {
            Log.err("Карта '@' не найдена.", args[0]);
        }
    }
}
