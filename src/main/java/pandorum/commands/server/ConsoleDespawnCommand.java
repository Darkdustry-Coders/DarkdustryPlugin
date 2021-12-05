package pandorum.commands.server;

import arc.util.Log;
import mindustry.gen.Groups;
import mindustry.gen.Unitc;
import pandorum.annotations.commands.ServerCommand;

public class ConsoleDespawnCommand {
    @ServerCommand(name = "despw", args = "", description = "Kill all units.")
    public static void run(final String[] args) {
        int amount = Groups.unit.size();
        Groups.unit.each(Unitc::kill);
        Log.info("Убито @ юнитов...", amount);
    }
}
