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

        allowCustomClients.set(true);
        showConnectMessages.set(false);
        logging.set(true);
        strict.set(true);
        antiSpam.set(true);
        autoPause.set(false);

        interactRateWindow.set(1);
        interactRateLimit.set(25);
        interactRateKick.set(50);
        messageRateLimit.set(1);
        messageSpamKick.set(5);
        packetSpamLimit.set(500);
        snapshotInterval.set(200);

        enableVotekick.set(config.mode.enableVotekick);

        if (config.mode.enableRtv) welcomeMessageCommands.add("rtv");
        if (config.mode.enableVnw) welcomeMessageCommands.add("vnw");
    }

    public enum Gamemode {
        anarchy {{
            enableVotekick = false;
        }},

        attack,

        castle {{
            isDefault = false;
            enableVnw = false;
        }},

        crawler {{
            isDefault = false;
            enableVnw = false;
        }},

        hexed {{
            isDefault = false;
            enableRtv = false;
            enableVnw = false;
            enableVotekick = false;
        }},

        hub {{
            isDefault = false;
            enableRtv = false;
            enableVnw = false;
            enableVotekick = false;
        }},

        msgo {{
            isDefault = false;
            enableRtv = false;
            enableVnw = false;
        }},

        pvp {{
            enableVnw = false;
        }},

        sandbox,
        survival,
        tower;

        public boolean isDefault = true;
        public boolean enableRtv = true;
        public boolean enableVnw = true;
        public boolean enableVotekick = true;
    }
}