package darkdustry.components;

import arc.func.Boolc;
import arc.util.Http;

import static darkdustry.PluginVars.config;

public class AntiVpn {

    public static void checkIp(String ip, Boolc boolc) {
        Http.get("https://blackbox.p.rapidapi.com/v1/" + ip)
                .header("X-RapidAPI-Key", config.antiVpnApiKey)
                .header("X-RapidAPI-Host", "blackbox.p.rapidapi.com")
                .error(e -> boolc.get(false))
                .submit(response -> boolc.get(response.getResultAsString().equals("Y")));
    }
}