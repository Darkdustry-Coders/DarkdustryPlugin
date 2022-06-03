package pandorum;

import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.mod.Plugin;

import static pandorum.PluginVars.clientCommands;
import static pandorum.PluginVars.serverCommands;

public class PandorumPlugin extends Plugin {

    @Override
    public void init() {
        Log.info("[Darkdustry] Инициализация плагина...");

        // Сначала загружаем конфигурацию
        Loader.loadConfig();
        Loader.load();
        Loader.init();

        Log.info("[Darkdustry] Инициализация плагина завершена.");
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        clientCommands = handler;
        Loader.registerClientCommands(clientCommands);
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        serverCommands = handler;
        Loader.registerServerCommands(serverCommands);
    }
}