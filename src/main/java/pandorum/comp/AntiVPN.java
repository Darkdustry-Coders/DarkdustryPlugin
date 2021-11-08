package pandorum.comp;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AntiVPN {
    private static Cache<String, Boolean> cache;
    private static String key;
    private static OkHttpClient client;
    public static String API_VERSION = "v2";

    public AntiVPN(String token) {
        cache = new Cache2kBuilder<String, Boolean>() {}
            .expireAfterWrite(7, TimeUnit.DAYS)
            .build();
        client = new OkHttpClient();
        key = token;
    }

    public void checkIp(String ip, Consumer<Boolean> callback) {
        if (cache.containsKey(ip)) {
            cache.expireAt(ip, new Date().getTime() + TimeUnit.DAYS.toMillis(7));
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
