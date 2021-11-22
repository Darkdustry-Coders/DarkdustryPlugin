package pandorum.commands.client;

import mindustry.gen.Player;
import pandorum.Misc;
import pandorum.comp.JavaScript;

import static mindustry.Vars.mods;
import static pandorum.Misc.bundled;

public class JavaScriptCommand {
    public static void run(final String[] args, final Player player) {
        if (Misc.adminCheck(player)) return;
        if (!JavaScript.allowScript(args[0])) {
            bundled(player, "commands.js.forbidden");
            return;
        }

        player.sendMessage("[lightgray]\uF120 [white]" + mods.getScripts().runConsole(args[0]));
    }
}
