package darkdustry.components;

import darkdustry.DarkdustryPlugin;

import static darkdustry.PluginVars.*;
import static darkdustry.components.Config.Gamemode.*;
import static mindustry.Vars.dataDirectory;
import static mindustry.net.Administration.Config.*;

public class Config {

    /** IP адрес Хаба. */
    public String hubIp = "darkdustry.tk";

    /** Порт Хаба. */
    public int hubPort = 6567;

    /** Url для подключения к базе данных. */
    public String mongoUrl = "url";

    /** Режим игры на сервере. */
    public Gamemode mode = survival;

    /** Токен бота, привязанного к серверу. */
    public String discordBotToken = "token";

    /** Префикс команд бота, привязанного к серверу. */
    public String discordBotPrefix = "prefix";

    /** ID канала в Discord, куда отправляются все сообщения. */
    public long discordBotChannelId = 0L;

    /** ID канала в Discord, куда отправляются подтверждения для администраторов. */
    public long discordAdminChannelId = 0L;

    /** ID роли администраторов в Discord. */
    public long discordAdminRoleId = 0L;

    public static void load() {
        var file = dataDirectory.child(configFileName);
        if (file.exists()) {
            config = gson.fromJson(file.reader(), Config.class);
            DarkdustryPlugin.info("Config loaded. (@)", file.absolutePath());
        } else {
            file.writeString(gson.toJson(config = new Config()));
            DarkdustryPlugin.info("Config file generated. (@)", file.absolutePath());
        }

        motd.set("off");
        showConnectMessages.set(false);
        logging.set(true);
        strict.set(true);
        antiSpam.set(true);
        interactRateWindow.set(1);
        interactRateLimit.set(25);
        interactRateKick.set(50);

        enableVotekick.set(config.mode != hexed && config.mode != hub);
        autoPause.set(config.mode.isDefault());
    }

    public enum Gamemode {
        attack, castle, crawler, hexed, hub, pvp, sandbox, survival, tower;

        public boolean isDefault() {
            return this == attack || this == pvp || this == sandbox || this == survival || this == tower;
        }

        public boolean useRtv() {
            return isDefault() || this == castle || this == crawler;
        }

        public boolean useVnw() {
            return isDefault() && this != pvp;
        }
    }
}