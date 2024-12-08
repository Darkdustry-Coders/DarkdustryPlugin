package darkdustry.config;

import useful.ConfigLoader;

import static darkdustry.PluginVars.*;
import static mindustry.Vars.*;

public class MatchmakingConfig {
    public static MatchmakingConfig config;

    public static boolean load() {
        if (!dataDirectory.child(matchmakingConfigFile).exists()) return false;
        config = ConfigLoader.load(MatchmakingConfig.class, matchmakingConfigFile);

        return true;
    }

    /** Inclusive start of port range for subservers */
    public int portRangeStart;

    /** Inclusive end of port range for subservers */
    public int portRangeEnd;

    /** Path to arena template */
    public String templatePath;

    /** Path to arenas */
    public String serversPath;

    /** Address to send players to */
    public String serversHost;
}
