package pandorum.commands.server;

import arc.util.Log;
import pandorum.comp.JavaScript;

import static mindustry.Vars.mods;

public class ConsoleJavaScriptCommand {
    public static void run(final String[] args) {
        if (!JavaScript.allowScript(args[0])) {
            Log.err("Выполнение этого скрипта запрещено.");
            return;
        }

        Log.info("&fi&lw&fb" + mods.getScripts().runConsole(args[0]));
    }
}
