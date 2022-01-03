package pandorum.commands.server;

import arc.util.Log;
import arc.util.OS;
import mindustry.core.Version;

public class VersionCommand {
    public static void run(final String[] args) {
        Log.info("На сервере установлена: Mindustry @-@ @ / build @", Version.number, Version.modifier, Version.type, Version.build + (Version.revision == 0 ? "" : "." + Version.revision));
        Log.info("Версия Java: @", OS.javaVersion);
    }
}
