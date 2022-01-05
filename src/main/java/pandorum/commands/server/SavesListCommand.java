package pandorum.commands.server;

import arc.files.Fi;
import arc.struct.Seq;
import arc.util.Log;

import static mindustry.Vars.saveDirectory;
import static mindustry.Vars.saveExtension;

public class SavesListCommand {
    public static void run(final String[] args) {
        Seq<Fi> savesList = Seq.with(saveDirectory.list()).filter(file -> file.extension().equals(saveExtension));
        if (savesList.isEmpty()) {
            Log.info("На сервере нет ни одного сохранения.");
        } else {
            Log.info("Сохранения сервера: (@)", savesList.size);
            savesList.each(save -> Log.info("  '@'", save.nameWithoutExtension()));
        }
        Log.info("Все сохранения находятся здесь: &fi@", saveDirectory.file().getAbsoluteFile().toString());
    }
}
