package rewrite.utils;

import arc.struct.ObjectMap;
import arc.util.Time;

public class Cooldowns {

    public static final ObjectMap<String, ObjectMap<String, Long>> cooldowns = new ObjectMap<>();
    public static final ObjectMap<String, Long> defaults = ObjectMap.of(
            "sync",     15L,
            "login",    900L,
            "votekick", 300L,
            "nominate", 150L,
            "alerts",   3L); // плохая идея для алертов

    public static boolean canRun(String uuid, String command) {
        if (!cooldowns.containsKey(uuid) || !cooldowns.get(uuid).containsKey(command)) return true;
        return cooldowns.get(uuid).get(command) <= Time.millis();
    }

    public static void run(String uuid, String command) {
        cooldowns.get(uuid, ObjectMap::new).put(command, Time.millis() + defaults.get(command) * 1000L);
    }
}
