package pandorum.commands.server;

import arc.func.Cons;
import arc.util.Log;
import arc.util.OS;
import mindustry.core.Version;

public class VersionCommand implements Cons<String[]> {
    public void get(String[] args) {
        Log.info("Version: Mindustry @-@ @ / build @", Version.number, Version.modifier, Version.type, Version.build + (Version.revision == 0 ? "" : "." + Version.revision));
        Log.info("Java Version: @", OS.javaVersion);
    }
}
