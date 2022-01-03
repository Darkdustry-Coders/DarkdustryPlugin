package pandorum.commands.server;

import arc.util.Log;

import static mindustry.Vars.modDirectory;
import static mindustry.Vars.mods;

public class ModsListCommand {
    public static void run(final String[] args) {
        if (mods.list().isEmpty()) {
            Log.info("На сервере нет ни одного загруженного мода.");
        } else {
            Log.info("Моды сервера: (@)", mods.list().size);
            mods.list().each(mod -> Log.info("  @ &fi@", mod.meta.displayName(), mod.meta.version));
        }
        Log.info("Все моды находятся здесь: &fi@", modDirectory.file().getAbsoluteFile().toString());
    }
}
