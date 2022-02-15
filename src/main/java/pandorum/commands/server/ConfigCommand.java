package pandorum.commands.server;

import arc.Core;
import arc.util.Log;
import arc.util.Strings;
import mindustry.net.Administration.Config;

public class ConfigCommand {
    public static void run(final String[] args) {
        if (args.length == 0) {
            Log.info("Все значения конфигурации:");
            for (Config c : Config.all) {
                Log.info("&lk| @: @", c.name(), "&lc&fi" + c.get());
                Log.info("&lk| | &lw" + c.description);
                Log.info("&lk|");
            }
            return;
        }

        try {
            Config c = Config.valueOf(args[0]);
            if (args.length == 1) {
                Log.info("Конфигурация '@' сейчас имеет значение '@'.", c.name(), c.get());
            } else {
                if (args[1].equalsIgnoreCase("default") || args[1].equalsIgnoreCase("reset")) {
                    c.set(c.defaultValue);
                } else if (c.isBool()) {
                    c.set(args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("true"));
                } else if (c.isNum()) {
                    c.set(Strings.parseInt(args[1], c.num()));
                } else if (c.isString()) {
                    c.set(args[1].replace("\\n", "\n"));
                }

                Log.info("Конфигурации '@' присвоено значение '@'.", c.name(), c.get());
                Core.settings.forceSave();
            }
        } catch (Exception e) {
            Log.err("Неизвестная конфигурация: '@'.", args[0]);
        }
    }
}
