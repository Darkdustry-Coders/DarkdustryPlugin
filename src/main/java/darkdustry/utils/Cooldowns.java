package darkdustry.utils;

import arc.struct.ObjectMap;
import arc.util.Time;
import mindustry.gen.Player;

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

    public static boolean canRun(Player player, String command) {
        if (player.admin || !cooldowns.containsKey(player.uuid()) || !cooldowns.get(player.uuid()).containsKey(command))
            return true;
        return cooldowns.get(player.uuid()).get(command) <= Time.millis();
    }

    public static void run(Player player, String command) {
        if (player.admin) return;
        cooldowns.get(player.uuid(), ObjectMap::new).put(command, Time.millis() + defaults.get(command, defaultCooldown));
    }
}