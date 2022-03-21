package pandorum.commands.server;

import arc.files.Fi;
import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.io.SaveIO;

import static mindustry.Vars.saveDirectory;

public class SavesListCommand implements Cons<String[]> {
    public void get(String[] args) {
        Seq<Fi> savesList = Seq.with(saveDirectory.list()).filter(SaveIO::isSaveValid);
        if (savesList.isEmpty()) {
            Log.info("На сервере нет ни одного сохранения.");
        } else {
            Log.info("Сохранения сервера: (@)", savesList.size);
            savesList.each(save -> Log.info("  '@'", save.nameWithoutExtension()));
        }
        Log.info("Все сохранения находятся здесь: &fi@", saveDirectory.absolutePath());
    }
}
