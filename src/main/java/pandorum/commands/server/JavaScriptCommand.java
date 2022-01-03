package pandorum.commands.server;

import arc.util.Log;

import static mindustry.Vars.mods;

public class JavaScriptCommand {
    public static void run(final String[] args) {
        Log.info("&fi&lw&fb@", mods.getScripts().runConsole(args[0]));
    }
}
