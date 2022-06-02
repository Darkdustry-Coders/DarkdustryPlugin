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

        // Регистрируем команды
        Loader.registerClientCommands();
        Loader.registerDiscordCommands();
        Loader.registerServerCommands();

        Log.info("[Darkdustry] Инициализация плагина завершена.");
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        clientCommands = handler;
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        serverCommands = handler;
    }
}