package darkdustry.config;

import arc.util.Log;
import useful.ConfigLoader;

import static darkdustry.PluginVars.*;
import static darkdustry.config.Config.Gamemode.*;
import static mindustry.Vars.*;
import static mindustry.net.Administration.Config.*;

public class Config {

    /** Конфигурация сервера. */
    public static Config config;

    public static void load() {
        config = ConfigLoader.load(Config.class, configFile);
        Log.info("Config loaded. (@)", dataDirectory.child(configFile).absolutePath());

        allowCustomClients.set(true);
        showConnectMessages.set(false);
        antiSpam.set(true);
        autoPause.set(false);

        interactRateWindow.set(1);
        interactRateLimit.set(25);
        interactRateKick.set(50);
        messageRateLimit.set(1);
        messageSpamKick.set(5);
        packetSpamLimit.set(500);
        snapshotInterval.set(200);
        roundExtraTime.set(10);
        maxLogLength.set(1024 * 1024);

        strict.set(config.mode.enableStrict);
        enableVotekick.set(config.mode.enableVotekick);

        if (config.mode.enableRtv) welcomeMessageCommands.add("rtv");
        if (config.mode.enableVnw) welcomeMessageCommands.add("vnw");
    }

    /** IP-адрес хаба. */
    public String hubIp = "darkdustry.net";

    /** Порт хаба. */
    public int hubPort = 6567;

    /** Порт сокета. */
    public int sockPort = 8306;

    /** Url для подключения к базе данных. */
    public String mongoUrl = "url";

    /** Режим игры на сервере. */
    public Gamemode mode = survival;

    public enum Gamemode {
        attack,

        castle {{
            isDefault = false;
            enableVnw = false;
        }},

        crawler {{
            isDefault = false;
            enableVnw = false;
        }},

        forts {{
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
            isMainServer = true;
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

        sandbox {{
            enableStrict = false;
        }},

        survival,
        tower;

        public boolean isDefault = true;
        public boolean isMainServer = false;

        public boolean enableRtv = true;
        public boolean enableVnw = true;
        public boolean enableStrict = true;
        public boolean enableVotekick = true;
    }
}