package pandorum.commands.server;

import arc.util.Log;
import mindustry.gen.Groups;
import mindustry.gen.Unitc;

import static mindustry.Vars.state;

public class DespawnCommand {
    public static void run(final String[] args) {
        if (state.isMenu()) {
            Log.err("Сервер отключен. Может быть, пора запустить его командой 'host'?");
            return;
        }

        int amount = Groups.unit.size();
        Groups.unit.each(Unitc::kill);
        Log.info("Убито @ юнитов...", amount);
    }
}
