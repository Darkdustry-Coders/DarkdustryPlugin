package pandorum.commands.server;

import arc.Core;
import arc.files.Fi;
import arc.util.Log;
import arc.util.Strings;
import mindustry.io.SaveIO;

import static mindustry.Vars.saveDirectory;
import static mindustry.Vars.saveExtension;
import static mindustry.Vars.state;

public class SaveCommand {
    public static void run(final String[] args) {
        if (state.isMenu()) {
            Log.err("Сервер отключен. Может быть, пора запустить его командой 'host'?");
            return;
        }

        Fi save = saveDirectory.child(Strings.format("@.@", args[0], saveExtension));

        Core.app.post(() -> SaveIO.save(save));
        Log.info("Карта сохранена в @", save.getAbsolutePath());
    }
}
