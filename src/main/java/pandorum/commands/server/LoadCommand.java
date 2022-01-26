package pandorum.commands.server;

import arc.Core;
import arc.files.Fi;
import arc.util.Log;
import mindustry.io.SaveIO;
import mindustry.io.SaveIO.SaveException;

import static mindustry.Vars.*;
import static pandorum.util.Search.findSave;

public class LoadCommand {
    public static void run(final String[] args) {
        if (!state.isMenu()) {
            Log.err("Сервер уже запущен. Используй 'stop', чтобы остановить его.");
            return;
        }

        Fi save = findSave(args[0]);
        if (save == null) {
            Log.err("Сохранение '@' не найдено.", args[0]);
            return;
        }

        Core.app.post(() -> {
            try {
                SaveIO.load(save);
                logic.play();

                Log.info("Сохранение загружено.");

                netServer.openServer();
            } catch (SaveException e) {
                Log.err(e);
            }
        });
    }
}
