package pandorum.commands.server;

import arc.util.Log;
import pandorum.PandorumPlugin;
import pandorum.annotations.commands.ServerCommand;
import pandorum.comp.Config;
import pandorum.discord.BotMain;

import static mindustry.Vars.dataDirectory;

public class ConsoleReloadCommand {
    @ServerCommand(name = "reload-config", args = "", description = "Reload the configuration file.")
    public static void run(final String[] args) {
        try {
            PandorumPlugin.config = PandorumPlugin.gson.fromJson(dataDirectory.child("config.json").readString(), Config.class);
            BotMain.start();
            Log.info("Файл конфигурации перезагружен...");
        } catch(Exception e) {
            Log.err("Возникла ошибка при перезагрузке файла конфигурации...");
            Log.err(e);
        }
    }
}
