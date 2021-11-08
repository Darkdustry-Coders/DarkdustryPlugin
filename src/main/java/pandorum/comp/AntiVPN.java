package pandorum.comp;

import arc.struct.ObjectMap;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class AntiVPN {
    private static String key;
    private static OkHttpClient client;
    public static String API_VERSION = "v2";
    private static ObjectMap<String, Boolean> cache;

    public AntiVPN(String token) {
        client = new OkHttpClient();
        cache = new ObjectMap<>();
        key = token;
    }

    public void checkIp(String ip, Consumer<Boolean> callback) {
        if (cache.containsKey(ip)) {
            callback.accept(cache.get(ip));
            return;
        }

        HttpUrl url = new HttpUrl.Builder()
            .scheme("https")
            .host("proxycheck.io")
            .addPathSegment(API_VERSION)
            .addPathSegment(ip)
            .addQueryParameter("key", key)
            .addQueryParameter("risk", "1")
            .addQueryParameter("vpn", "1")
            .build();
        Request request = new Request.Builder()
            .addHeader("accept", "application/json")
            .url(url)
            .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call request, @NotNull IOException e) {
                callback.accept(false);
            }

            @Override
            public void onResponse(@NotNull Call request, @NotNull Response response) throws IOException {
                JSONObject body = new JSONObject(response.body().string());
                JSONObject ipInfo = body.getJSONObject(ip);

                int risk = ipInfo.getInt("risk");
                String isProxy = ipInfo.getString("proxy");
                String ipType = ipInfo.getString("type");

                boolean isVPN = risk >= 66 || Objects.equals(isProxy, "yes")
                    || Set.of(
                        "tor", "socks", "socks4", "socks4a",
                        "socks5", "socks5h", "shadowsocks",
                        "compromised server", "inference engine",
                        "openvpn", "vpn"
                    ).contains(ipType.toLowerCase());
                cache.put(ip, isVPN);
                callback.accept(isVPN);
            }
        });
    }
}
