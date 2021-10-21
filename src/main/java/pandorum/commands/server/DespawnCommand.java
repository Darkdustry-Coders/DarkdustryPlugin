package pandorum.commands.server;

import arc.util.Log;
import mindustry.gen.Groups;
import mindustry.gen.Unitc;

public class DespawnCommand {
    public static void run(final String[] args) {
        int amount = Groups.unit.size();
        Groups.unit.each(Unitc::kill);
        Log.info("Ты убил @ юнитов!", amount);
    }
}
