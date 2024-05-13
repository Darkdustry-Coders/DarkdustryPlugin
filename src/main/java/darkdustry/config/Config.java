package darkdustry.config;

import arc.util.Log;
import mindustry.game.Team;
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
    }

    /** IP-адрес хаба. */
    public String hubIp = "darkdustry.net";

    /** Порт хаба. */
    public int hubPort = 6567;

    /** Порт сокета. */
    public int sockPort = 8306;

    /** Url для подключения к базе данных. */
    public String mongoUrl = "url";

    /** Путь до плагина. */
    public String pluginSource = "path";

    /** Max units that can exist on a map (except for players) */
    public int maxUnitsTotal = -1;

    /** Make unit cap be affected solely by the core amount */
    public boolean straightforwardUnitCap = true;

    /** Режим игры на сервере. */
    public Gamemode mode = hub;

    public enum Gamemode {
        attack("Attack"),

        castle("Castle Wars") {{
            isDefault = false;
            enableVnw = false;
            enableSurrender = true;
            enableSpectate = true;
        }},

        crawler("Crawler Arena") {{
            isDefault = false;
            enableVnw = false;
        }},

        forts("Forts") {{
            enableVnw = false;
            enableSurrender = true;
            enableSpectate = true;
            enable1va = true;
        }},

        hexed("Hexed PvP") {{
            disableAttackMode = true;
            isDefault = false;
            enableRtv = false;
            enableVnw = false;
            enableVotekick = false;
        }},

        hub("Hub") {{
            isDefault = false;
            isMainServer = true;
            enableRtv = false;
            enableVnw = false;
            enableVotekick = false;
        }},

        msgo("MS:GO") {{
            isDefault = false;
            enableRtv = false;
            enableVnw = false;
        }},

        pvp("PvP") {{
            enableVnw = false;
            enableSurrender = true;
            enableSpectate = true;
        }},

        sandbox("Sandbox") {{
            enableStrict = false;
        }},

        survival("Survival"),
        tower("Tower Defense") {{
            disableCrashDamage = true;
        }},

        test("Test") {{
            enableVnw = false;
            enableStrict = false;
            enableSurrender = false;
            enableSpectate = true;
        }},

        sandboxPVP("SandboxPvP") {{
            enableVnw = false;
            enableSurrender = false;
            enableSpectate = true;
        }};

        public final String displayName;

        public boolean isDefault = true;
        public boolean isMainServer = false;

        public boolean enableRtv = true;
        public boolean enableVnw = true;
        public boolean enableStrict = true;
        public boolean enableVotekick = true;
        public boolean enableSurrender = false;
        public boolean enableSpectate = false;
        public boolean enable1va = false;

        public boolean disableAttackMode = false;
        public boolean disableCrashDamage = false;

        public Team spectatorTeam = Team.get(69);

        Gamemode(String displayName) {
            this.displayName = displayName;
        }

        public static String getDisplayName(String name) {
            return Gamemode.valueOf(name).displayName;
        }
    }
}