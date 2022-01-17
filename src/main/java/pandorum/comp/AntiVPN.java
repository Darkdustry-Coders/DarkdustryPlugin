package pandorum.comp;

import arc.func.Cons;
import arc.struct.Seq;
import com.google.gson.JsonObject;
import okhttp3.*;
import okhttp3.Request.Builder;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static pandorum.PluginVars.*;

public class AntiVPN {

    private static final Builder antiVpnRequest = new Builder().addHeader("accept", "application/json");

    public static void checkIp(String ip, Cons<Boolean> cons) {
        if (antiVpnCache.containsKey(ip)) {
            cons.get(antiVpnCache.get(ip));
            return;
        }

        HttpUrl url = new HttpUrl.Builder().scheme("https").host("proxycheck.io").addPathSegment("v2").addPathSegment(ip).addQueryParameter("key", config.antiVpnToken).addQueryParameter("risk", "1").addQueryParameter("vpn", "1").build();
        Request request = antiVpnRequest.url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                cons.get(false);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                JsonObject ipInfo = gson.fromJson(response.body().string(), JsonObject.class).getAsJsonObject(ip);

                boolean isDangerous = ipInfo.get("risk").getAsInt() > 66 || Seq.with("tor", "socks", "socks4", "socks4a", "socks5", "socks5h", "shadowsocks", "openvpn", "vpn").contains(ipInfo.get("type").getAsString().toLowerCase());

                antiVpnCache.put(ip, isDangerous);
                cons.get(isDangerous);
            }
        });
    }
}
