package darkdustry.components;

import arc.util.*;
import arc.util.serialization.Jval;

public class AntiVpn {

    public static void checkIp(String ip, Runnable runnable) {
        Http.get("https://proxycheck.io/v2/" + ip + "?vpn=1&asn=1")
                .error(Log::debug)
                .submit(response -> {
                    var result = Jval.read(response.getResultAsString()).get(ip);
                    if (result == null) return;

                    if (result.getString("type").equals("VPN"))
                        runnable.run();
                });
    }
}