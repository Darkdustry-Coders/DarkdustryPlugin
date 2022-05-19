package pandorum.commands.server;

import arc.Core;
import arc.files.Fi;
import arc.func.Cons;
import arc.util.Log;
import arc.util.Strings;
import mindustry.io.SaveIO;

import static mindustry.Vars.*;

public class SaveCommand implements Cons<String[]> {
    public void get(String[] args) {
        if (state.isMenu()) {
            Log.err("Сервер отключен. Может быть, пора запустить его командой 'host'?");
            return;
        }

        Fi save = saveDirectory.child(Strings.format("@.@", args[0], saveExtension));

        Core.app.post(() -> {
            SaveIO.save(save);
            Log.info("Карта сохранена в @", save.absolutePath());
        });
    }
}
