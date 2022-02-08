package pandorum.components;

import arc.func.Cons;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mindustry.gen.Player;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static pandorum.PluginVars.*;

public class Translator {

    private static final Request.Builder translatorRequest = new Request.Builder()
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

    private static final Request.Builder languagesRequest = new Request.Builder()
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
            .addHeader("if-none-match", "W/\"aec6-7FjvQqCRl/1E+dvnCAlbAedDteg\"");

    public static void translate(String text, String locale, Cons<String> cons) {
        Request request = translatorRequest.post(new FormBody.Builder()
                .add("to", locale)
                .add("text", text)
                .add("platform", "dp")
                .build()
        ).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                cons.get("");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                cons.get(gson.fromJson(response.body().string(), JsonObject.class).get("result").getAsString());
            }
        });
    }

    public static void loadLanguages() {
        client.newCall(languagesRequest.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {}

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                for (JsonElement languageElement : gson.fromJson(response.body().string(), JsonObject.class).get("result").getAsJsonArray()) {
                    JsonObject language = languageElement.getAsJsonObject();
                    codeLanguages.put(language.get("code_alpha_1").getAsString(), language.get("full_code").getAsString());
                }
            }
        });
    }

    public static String getLocale(Player player, String locale) {
        return codeLanguages.get(locale.equals("auto") ? player.locale : locale, codeLanguages.get(defaultLocale));
    }
}
