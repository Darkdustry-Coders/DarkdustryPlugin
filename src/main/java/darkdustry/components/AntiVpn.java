package darkdustry.components;

import arc.struct.ObjectMap;
import arc.util.*;
import arc.util.serialization.Jval;

public class AntiVpn {

    private static final ObjectMap<String, Boolean> cache = new ObjectMap<>();

    public static void checkIp(String ip, Runnable runnable) {
        if (cache.containsKey(ip)) {
            if (cache.get(ip)) runnable.run();
            return;
        }

        Http.get("https://funkemunky.cc/vpn?ip=" + ip)
                .error(Log::debug)
                .submit(response -> {
                    var json = Jval.read(response.getResultAsString());
                    boolean vpn = json.getBool("proxy", false);

                    cache.put(ip, vpn);
                    if (vpn) runnable.run();
                });
    }
}