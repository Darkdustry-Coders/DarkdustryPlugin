package darkdustry.config;

import arc.struct.IntSeq;
import arc.util.Log;
import mindustry.game.Team;
import mindustry.net.Administration;
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

        if (config.overridePort != -1)
            Administration.Config.port.set(config.overridePort);

        strict.set(config.mode.enableStrict);
        enableVotekick.set(config.mode.enableVotekick);
    }

    /** IP-адрес хаба. */
    public String hubIp = "mindurka.fun";

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

    /** Allow maps to set special settings */
    public boolean allowSpecialSettings = false;

    /** Use PolymerAI for monos and polys instead */
    public boolean overrideMonoAi = false;

    /** Amount of backups to keep. 0 or less to disable */
    public int maxBackupCount = 2;

    /** Delay between backups */
    public int backupDelaySec = 60 * 5;

    /** Value to replace port with */
    public int overridePort = -1;

    /** IDs that are allowed to join the server */
    public IntSeq whitelist = IntSeq.with();

    /** Whether instead of blocking connection, player should be forced into spectator mode */
    public boolean whitelistKickSpectator = false;

    /** ID of this server */
    public int serverId = -1;

    /** Режим игры на сервере. */
    public Gamemode mode = hub;

    public enum Gamemode {
        attack("Attack") {{
            enableSpectate = true;
        }},

        castle("Castle Wars") {{
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
            rememberTeams = true;
        }},

        hexed("Hexed PvP") {{
            postSetup = () -> state.rules.attackMode = false;
            isDefault = false;
            enableRtv = true;
            enableVnw = false;
            enableVotekick = false;
            restartOnNoPlayers = false;
        }},

        hub("Hub") {{
            isDefault = false;
            isMainServer = true;
            enableRtv = false;
            enableVnw = false;
            enableVotekick = false;
            restartOnNoPlayers = false;
        }},

        msgo("MS:GO") {{
            isDefault = false;
            enableRtv = false;
            enableVnw = false;
            restartOnNoPlayers = false;
        }},

        pvp("PvP") {{
            enableVnw = false;
            enableSurrender = true;
            enableSpectate = true;
            enable1va = true;
        }},

        sandbox("Sandbox") {{
            postSetup = () -> state.rules.damageExplosions = false;
            enableStrict = false;
            enableVnw = false;
        }},

        survival("Survival") {{
            enableSpectate = true;
        }},
        zombies("Zombies"),
        tower("Tower Defense") {{
            enableSpectate = true;
            postSetup = () -> state.rules.unitCrashDamageMultiplier = 0.0f;
        }},

        test("Test") {{
            enableVnw = false;
            enableStrict = false;
            enableSurrender = false;
            enableSpectate = true;
        }},

        spvp("SandboxPvP") {{
            enableVnw = false;
            enableSurrender = true;
            enableSpectate = true;
        }},

        rankedpvplobby("Ranked PvP") {{
            isDefault = false;
            enableRtv = false;
            enableVnw = false;
            enableVotekick = false;
            restartOnNoPlayers = false;
            rememberTeams = false;
            maskUsernames = true;
        }},

        rankedpvparena("Internal Server") {{
            isDefault = false;
            enableRtv = false;
            enableVnw = false;
            enableVotekick = false;
            maskUsernames = true;
        }},

        rankedfortslobby("Ranked Forts") {{
            isDefault = false;
            enableRtv = false;
            enableVnw = false;
            enableVotekick = false;
            restartOnNoPlayers = false;
            rememberTeams = false;
            maskUsernames = true;
        }},

        rankedfortsarena("Internal Server") {{
            isDefault = false;
            enableRtv = false;
            enableVnw = false;
            enableVotekick = false;
            maskUsernames = true;
        }},

        ;

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
        public boolean rememberTeams = false;
        public boolean restartOnNoPlayers = true;
        public boolean maskUsernames = false;

        public Funcv postSetup = null;

        public final Team spectatorTeam = Team.get(69);

        Gamemode(String displayName) {
            this.displayName = displayName;
        }

        public static String getDisplayName(String name) {
            return Gamemode.valueOf(name).displayName;
        }
    }

    public interface Funcv {
        void get();
    }
}
