package pandorum.comp;

import arc.func.Cons;
import arc.struct.StringMap;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

public class Translator {

    public static final StringMap codeLanguages = new StringMap();
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

    public Translator() throws IOException {
        this.client = new OkHttpClient();

        JSONArray languages = getAllLanguages();
        for (int i = 0; i < languages.length(); i++) {
            JSONObject language = languages.getJSONObject(i);
            codeLanguages.put(language.getString("code_alpha_1"), language.getString("full_code"));
        }
    }

    public void translate(String text, String lang, Cons<String> callback) {
        String language = codeLanguages.get(lang, codeLanguages.get("en"));

        RequestBody formBody = new FormBody.Builder()
                .add("to", language)
                .add("text", text)
                .add("platform", "dp")
                .build();

        Request request = requestBuilder
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.get("");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                JSONObject translated = new JSONObject(Objects.requireNonNull(response.body()).string());
                String translatedText = translated.optString("result", text);
                try {
                    callback.get(translatedText);
                } finally {
                    if (response.body() != null) response.close();
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
