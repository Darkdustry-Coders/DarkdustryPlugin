package pandorum.commands.server;

import arc.Core;
import arc.util.Log;
import mindustry.gen.Groups;

import static mindustry.Vars.state;
import static pandorum.PluginVars.mapPlaytime;
import static pandorum.PluginVars.serverUptime;
import static pandorum.util.Utils.formatDuration;

public class StatusCommand {
    public static void run(final String[] args) {
        if (state.isMenu()) {
            Log.info("Сервер отключен. Может быть, пора запустить его командой 'host'?");
        } else {
            Log.info("Статус сервера:");
            Log.info("  Сервер онлайн уже: @", formatDuration(serverUptime));
            Log.info("  Карта: &@", state.map.name());
            Log.info("  Время игры на этой карте: @", formatDuration(mapPlaytime));

            if (state.rules.waves) Log.info("  @ волна, @ секунд до следующей волны.", state.wave, (int) (state.wavetime / 60));
            Log.info("  @ юнитов / @ вражеских юнитов.", Groups.unit.size(), state.enemies);
            Log.info("  @ TPS, @ MB памяти занято.", Core.graphics.getFramesPerSecond(), Core.app.getJavaHeap() / 1024 / 1024);

            if (Groups.player.size() > 0) {
                Log.info("  Игроки: (@)", Groups.player.size());
                Groups.player.each(player -> Log.info("    '@' / '@'", player.name, player.uuid()));
            } else {
                Log.info("  На сервере нет игроков.");
            }
        }
    }
}
