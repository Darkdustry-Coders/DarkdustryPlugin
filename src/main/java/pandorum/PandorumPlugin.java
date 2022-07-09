package pandorum;

import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.mod.Plugin;

import static pandorum.PluginVars.clientCommands;
import static pandorum.PluginVars.serverCommands;

@SuppressWarnings({"unused", "hentai"})
public class PandorumPlugin extends Plugin {

    @Override
    public void init() {
        Log.info("[Darkdustry] Начинается инициализация плагина.");

        // Сначала загружаем конфигурацию
        Loader.loadConfig();
        Loader.load();
        Loader.init();

        Log.info("[Darkdustry] Инициализация плагина завершена.");
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        Loader.registerClientCommands(clientCommands = handler);
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        Loader.registerServerCommands(serverCommands = handler);
    }
}