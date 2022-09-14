package darkdustry.components;

import arc.files.Fi;
import darkdustry.DarkdustryPlugin;

import static darkdustry.PluginVars.*;
import static mindustry.Vars.dataDirectory;
import static mindustry.net.Administration.Config.*;

public class Config {

    /** IP адрес Хаба. */
    public String hubIp = "darkdustry.tk";

    /** Порт Хаба. */
    public int hubPort = 6567;

    /** Строка подключения к базе данных. */
    public String mongoUrl = "url";

    /** Режим игры на сервере. */
    public Gamemode mode = Gamemode.survival;

    /** Токен бота, привязанного к серверу. */
    public String discordBotToken = "token";

    /** ID сервера в Discord. */
    public long discordGuildId = 0L;

    /** ID канала в Discord, куда отправляются все сообщения. */
    public long discordBotChannelId = 0L;

    /** ID канала в Discord, куда отправляются подтверждения для администраторов. */
    public long discordAdminChannelId = 0L;

    /** ID роли администраторов в Discord. */
    public long discordAdminRoleId = 0L;

    public static void load() {
        Fi file = dataDirectory.child(configFileName);
        if (file.exists()) {
            config = gson.fromJson(file.reader(), Config.class);
            DarkdustryPlugin.info("Config loaded. (@)", file.absolutePath());
        } else {
            file.writeString(gson.toJson(config = new Config()));
            DarkdustryPlugin.info("Config file generated. (@)", file.absolutePath());
        }

        motd.set("off");
        interactRateWindow.set(3);
        interactRateLimit.set(50);
        interactRateKick.set(1000);
        showConnectMessages.set(false);
        logging.set(true);
        strict.set(true);
    }

    public enum Gamemode {
        attack, castle, crawler, hexed, hub, pvp, sandbox, survival, tower, industry;

        public boolean isDefault() {
            return defaultModes.contains(this);
        }
    }
}
