package darkdustry.utils;

import arc.struct.ObjectMap;
import arc.util.Time;

import static darkdustry.PluginVars.defaultCooldown;

public class Cooldowns {

    public static final ObjectMap<String, ObjectMap<String, Long>> cooldowns = new ObjectMap<>();
    public static final ObjectMap<String, Long> defaults = ObjectMap.of(
            "sync", 15000L,
            "votekick", 300000L,
            "login", 900000L,
            "rtv", 30000L,
            "vnw", 30000L,
            "savemap", 90000L,
            "loadsave", 90000L
    );

    public static boolean canRun(String uuid, String command) {
        if (!cooldowns.containsKey(uuid) || !cooldowns.get(uuid).containsKey(command)) return true;
        return cooldowns.get(uuid).get(command) <= Time.millis();
    }

    public static void run(String uuid, String command) {
        cooldowns.get(uuid, ObjectMap::new).put(command, Time.millis() + defaults.get(command, defaultCooldown));
    }
}