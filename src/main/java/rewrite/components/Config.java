package rewrite.components;

import arc.files.Fi;
import mindustry.io.JsonIO;
import rewrite.DarkdustryPlugin;

import static mindustry.Vars.*;
import static mindustry.net.Administration.Config.*;
import static rewrite.PluginVars.*;

public class Config {

    /** IP адрес Хаба. */
    public String hubIp = "darkdustry.tk"; // TODO это айпи ХАБА а не всего сервера, вдруг я вынесу его на отдельный хост. Не надо его юзать там где не хаб

    /** Порт Хаба. */
    public int hubPort = 6567;

    /** Имя пользователя базы данных. */
    public String jedisIp = "localhost";

    /** Пароль пользователя базы данных. */
    public int jedisPort = 6379;

    /** Режим игры на сервере. */
    public Gamemode mode = Gamemode.survival;

    /** Токен бота, привязанного к серверу. */
    public String discordBotToken = "token";

    /** Префикс бота, привязанного к серверу. */
    public String discordBotPrefix = "prefix";

    /** ID сервера в Discord. */
    public long discordGuildId = 0L;

    /** ID канала в Discord, куда отправляются все сообщения. */
    public long discordBotChannelId = 0L;

    /** ID канала в Discord, куда отправляются подтверждения для администраторов. */
    public long discordAdminChannelId = 0L;

    /** ID роли администраторов в Discord. */
    public long discordAdminRoleId = 0L;

    /** Ключ API для переводчика чата. */
    public String translatorApiKey = "key";

    public static void load() {
        Fi file = dataDirectory.child(configFileName);
        if (file.exists()) {
            config = JsonIO.json.fromJson(Config.class, file.reader());
            DarkdustryPlugin.info("Конфигурация загружена. (@)", file.absolutePath());
        } else {
            file.writeString(JsonIO.json.toJson(config = new Config()));
            DarkdustryPlugin.info("Файл конфигурации сгенерирован. (@)", file.absolutePath());
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
