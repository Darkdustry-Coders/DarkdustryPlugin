package pandorum;

import arc.ApplicationListener;
import arc.util.Log;

public class Main implements ApplicationListener {

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
    public void dispose() {
        Log.info("[Darkdustry] Выключение плагина...");
    }
}
