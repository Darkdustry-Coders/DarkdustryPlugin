package pandorum.features;

import static pandorum.PluginVars.config;
import static pandorum.PluginVars.defaultModes;

public class History {

    // TODO вынести сюда все что связано с history

    public static boolean enabled() {
        return defaultModes.contains(config.mode);
    }


}
