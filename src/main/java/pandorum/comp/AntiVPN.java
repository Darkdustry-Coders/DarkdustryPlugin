package pandorum.comp;

import arc.func.Cons;
import arc.struct.ObjectMap;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

public class AntiVPN {

    private final ObjectMap<String, Boolean> cache;
    private final String token;
    private final OkHttpClient client;

    private final Request.Builder requestBuilder = new Request.Builder()
            .addHeader("accept", "application/json");

    public AntiVPN(String token) {
        this.cache = new ObjectMap<>();
        this.client = new OkHttpClient();
        this.token = token;
    }

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
                .addQueryParameter("key", token)
                .addQueryParameter("risk", "1")
                .addQueryParameter("vpn", "1")
                .build();

        Request request = requestBuilder
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call request, @NotNull IOException e) {
                cons.get(false);
            }

            @Override
            public void onResponse(@NotNull Call request, @NotNull Response response) throws IOException {
                JSONObject body = new JSONObject(Objects.requireNonNull(response.body()).string());
                JSONObject ipInfo = body.getJSONObject(ip);

                int risk = ipInfo.getInt("risk");
                String type = ipInfo.getString("type");

                boolean isDangerous = risk > 66 || Set.of(
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
