package pandorum.comp;

import arc.func.Cons;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static pandorum.PandorumPlugin.gson;
import static pandorum.PluginVars.config;

public class AntiVPN {

    private static final ObjectMap<String, Boolean> cache = new ObjectMap<>();
    private static final OkHttpClient client = new OkHttpClient();
    private static final Request.Builder requestBuilder = new Request.Builder().addHeader("accept", "application/json");

    public void checkIp(String ip, Cons<Boolean> cons) {
        if (cache.containsKey(ip)) {
            cons.get(cache.get(ip));
            return;
        }

        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("proxycheck.io")
                .addPathSegment("v2")
                .addPathSegment(ip)
                .addQueryParameter("key", config.antiVpnToken)
                .addQueryParameter("risk", "1")
                .addQueryParameter("vpn", "1")
                .build();

        Request request = requestBuilder.url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call request, @NotNull IOException e) {
                cons.get(false);
            }

            @Override
            public void onResponse(@NotNull Call request, @NotNull Response response) throws IOException {
                JsonObject ipInfo = gson.fromJson(response.body().string(), JsonObject.class).getAsJsonObject(ip);

                int risk = ipInfo.get("risk").getAsInt();
                String type = ipInfo.get("type").getAsString();

                boolean isDangerous = risk > 66 || Seq.with(
                        "tor", "socks", "socks4", "socks4a",
                        "socks5", "socks5h", "shadowsocks",
                        "openvpn", "vpn"
                ).contains(type.toLowerCase());

                cache.put(ip, isDangerous);
                cons.get(isDangerous);
            }
        });
    }
}
