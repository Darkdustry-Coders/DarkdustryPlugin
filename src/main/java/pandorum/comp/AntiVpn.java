package pandorum.comp

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Consumer;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

public class AntiVPN {
    private String token;
    private OkHttpClient client;
    public static String API_VERSION = "v2";

    public AntiVPN(String token) {
        this.client = new OkHttpClient();
        this.token = token;
    }

    public void isDangerousIp(String ip, Consumer<Boolean> callback) {
        HttpUrl url = new HttpUrl.Builder()
            .scheme("https")
            .host("proxycheck.io")
            .addPathSegment(API_VERSION)
            .addPathSegment(ip)
            .addQueryParameter("key", token)
            .addQueryParameter("risk", "1")
            .addQueryParameter("vpn", "1")
            .build();
        Request request = new Request.Builder()
            .addHeader("accept", "application/json")
            .url(url)
            .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.accept(false);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                JSONObject body = new JSONObject(response.body().string());
                JSONObject ipInfo = body.getJSONObject(ip);

                int risk = ipInfo.getInt("risk");
                String isProxy = ipInfo.getString("proxy");
                String ipType = ipInfo.getString("type");

                callback.accept(
                        risk >= 66 || isProxy == "yes" || Set.of(
                        "tor", "socks", "socks4", "socks4a",
                        "socks5", "socks5h", "shadowsocks",
                        "compromised server", "inference engine",
                        "openvpn", "vpn"
                    ).contains(ipType.toLowerCase())
                );
            }
        });
    }
}
