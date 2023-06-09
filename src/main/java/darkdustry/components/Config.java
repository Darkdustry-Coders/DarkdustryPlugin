package darkdustry.components;

import darkdustry.DarkdustryPlugin;

import static darkdustry.PluginVars.*;
import static darkdustry.components.Config.Gamemode.*;
import static mindustry.Vars.*;
import static mindustry.net.Administration.Config.*;

public class Config {

    /** IP адрес Хаба. */
    public String hubIp = "darkdustry.net";

    /** Порт Хаба. */
    public int hubPort = 6567;

    /** Url для подключения к базе данных. */
    public String mongoUrl = "url";

    /** Режим игры на сервере. */
    public Gamemode mode = survival;

    /** Токен бота, привязанного к серверу. */
    public String discordBotToken = "token";

    /** Токен бота, привязанного к серверу. */
    public String discordBotPrefix = "prefix";

    /** ID сервера в Discord, к которому привязан бот. */
    public long discordBotGuildId = 0L;

    /** ID канала в Discord, куда отправляются все сообщения. */
    public long discordBotChannelId = 0L;

    /** ID канала в Discord, куда отправляются баны. */
    public long discordBanChannelId = 0L;

    /** ID канала в Discord, куда отправляются подтверждения для администраторов. */
    public long discordAdminChannelId = 0L;

    /** ID роли администраторов в Discord. */
    public long discordAdminRoleId = 0L;

    /** ID роли картоделов в Discord. */
    public long discordMapReviewerRoleId = 0L;

    public static void load() {
        var file = dataDirectory.child(configFileName);
        if (file.exists()) {
            config = gson.fromJson(file.reader(), Config.class);
            DarkdustryPlugin.info("Config loaded. (@)", file.absolutePath());
        } else {
            file.writeString(gson.toJson(config = new Config()));
            DarkdustryPlugin.info("Config generated. (@)", file.absolutePath());
        }

        motd.set("off");
        showConnectMessages.set(false);
        logging.set(true);
        strict.set(true);
        antiSpam.set(true);
        interactRateWindow.set(1);
        interactRateLimit.set(25);
        interactRateKick.set(50);
        messageRateLimit.set(1);
        messageSpamKick.set(5);
        packetSpamLimit.set(500);
        snapshotInterval.set(200);

        enableVotekick.set(config.mode != hexed && config.mode != industry && config.mode != hub);
        autoPause.set(false);

        if (config.mode.useRtv()) welcomeMessageCommands.add("rtv");
        if (config.mode.useVnw()) welcomeMessageCommands.add("vnw");
    }

    public enum Gamemode {
        attack, castle, crawler, hexed, hub, industry, pvp, sandbox, survival, tower;

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