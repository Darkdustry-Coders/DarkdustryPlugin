package darkdustry.components;

import arc.func.Boolc;
import arc.util.Http;

public class AntiVpn {

    public static void checkIp(String ip, Boolc boolc) {
        Http.get("https://blackbox.p.rapidapi.com/v1/" + ip)
                .header("X-RapidAPI-Key", "4806eaec32mshfb4299cba6503c0p19bda2jsndc531e46c517")
                .header("X-RapidAPI-Host", "blackbox.p.rapidapi.com")
                .error(e -> boolc.get(false))
                .submit(response -> boolc.get(response.getResultAsString().equals("Y")));
    }
}