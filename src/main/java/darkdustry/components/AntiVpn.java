package darkdustry.components;

import arc.util.*;

import static darkdustry.PluginVars.*;

public class AntiVpn {

    public static void checkIp(String ip, Runnable runnable) {
        Http.get(antiVpnApiUrl + ip)
                .header("X-RapidAPI-Key", config.antiVpnApiKey)
                .header("X-RapidAPI-Host", antiVpnApiHost)
                .error(Log::debug)
                .submit(response -> {
                    var json = reader.parse(response.getResultAsString());
                    var detection = json.get("detection");

                    boolean isVpn = detection.getBoolean("bogon") ||
                            detection.getBoolean("cloud") ||
                            detection.getBoolean("hosting") ||
                            detection.getBoolean("proxy") ||
                            detection.getBoolean("spamhaus") ||
                            detection.getBoolean("tor");

                    if (isVpn)
                        runnable.run();
                });
    }
}