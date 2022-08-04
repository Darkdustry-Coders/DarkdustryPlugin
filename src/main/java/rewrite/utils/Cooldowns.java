package rewrite.utils;

import arc.struct.ObjectMap;
import arc.util.Time;

public class Cooldowns {

    public static final ObjectMap<String, ObjectMap<String, Long>> cooldowns = new ObjectMap<>();
    public static final ObjectMap<String, Long> defaults = ObjectMap.of(
            "sync",     15,
            "login",    900,
            "kick",     300,
            "nominate", 150,
            "alerts",   3);

    public static boolean runnable(String uuid, String cmd) {
        if (!cooldowns.containsKey(uuid) || !cooldowns.get(uuid).containsKey(cmd)) return true;
        return cooldowns.get(uuid).get(cmd) <= Time.millis();
    }

    public static void runned(String uuid, String cmd) {
        if (!cooldowns.containsKey(uuid)) cooldowns.put(uuid, new ObjectMap<>());
        cooldowns.get(uuid).put(cmd, Time.millis() + defaults.get(cmd) * 1000L);
    }
}
