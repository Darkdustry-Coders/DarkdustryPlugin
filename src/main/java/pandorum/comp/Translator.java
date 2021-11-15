package pandorum.comp;

import arc.util.Log;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import pandorum.PandorumPlugin;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

public class Translator {
    private final OkHttpClient client;

    private final Request.Builder requestBuilder = new Request.Builder()
            .url("https://api-b2b.backenster.com/b1/api/v3/translate/")
            .addHeader("accept", "application/json, text/javascript, */*; q=0.01")
            .addHeader("accept-language", "ru,en;q=0.9")
            .addHeader("authorization", "Bearer a_25rccaCYcBC9ARqMODx2BV2M0wNZgDCEl3jryYSgYZtF1a702PVi4sxqi2AmZWyCcw4x209VXnCYwesx")
            .addHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
            .addHeader("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"90\", \"Yandex\";v=\"90\"")
            .addHeader("sec-ch-ua-mobile", "?0")
            .addHeader("sec-fetch-dest", "empty")
            .addHeader("sec-fetch-mode", "cors")
            .addHeader("sec-fetch-site", "cross-site");

    public Translator() {
        this.client = new OkHttpClient();
    }

    public void translate(String text, String dest_lang, Consumer<JSONObject> callback) {
        String destAlphaLang = PandorumPlugin.codeLanguages.containsKey(dest_lang) ? PandorumPlugin.codeLanguages.get(dest_lang) : PandorumPlugin.codeLanguages.get("en");

        RequestBody formBody = new FormBody.Builder()
                .add("to", destAlphaLang)
                .add("text", text)
                .add("platform", "dp")
                .build();

        Request request = requestBuilder
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.err(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                JSONObject parsedBody = response.isSuccessful() ? new JSONObject(Objects.requireNonNull(response.body()).string()) : new JSONObject("{}");

                try {
                    callback.accept(parsedBody);
                } catch(Exception e) {
                    Log.err(e);
                } finally {
                    if (response.body() != null) Objects.requireNonNull(response.body()).close();
                }
            }
        });
    }

    public JSONArray getAllLanguages() throws IOException {
        Request request = new Request.Builder()
                .url("https://api-b2b.backenster.com/b1/api/v3/getLanguages?platform=dp")
                .get()
                .addHeader("accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("accept-language", "ru,en;q=0.9")
                .addHeader("authorization", "Bearer a_25rccaCYcBC9ARqMODx2BV2M0wNZgDCEl3jryYSgYZtF1a702PVi4sxqi2AmZWyCcw4x209VXnCYwesx")
                .addHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                .addHeader("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"90\", \"Yandex\";v=\"90\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-site", "cross-site")
                .addHeader("if-none-match", "W/\"aec6-7FjvQqCRl/1E+dvnCAlbAedDteg\"")
                .build();

        Response response = client.newCall(request).execute();
        return response.isSuccessful() ? new JSONObject(Objects.requireNonNull(response.body()).string()).getJSONArray("result") : new JSONArray("[]");
    }
}
