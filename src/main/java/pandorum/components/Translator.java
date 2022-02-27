package pandorum.components;

import arc.func.Cons;
import arc.util.Http;
import arc.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import static pandorum.PluginVars.codeLanguages;
import static pandorum.PluginVars.gson;

public class Translator {

    public static void translate(String text, String to, Cons<String> cons) {
        JsonObject json = new JsonObject();
        json.addProperty("to", to);
        json.addProperty("text", text);
        json.addProperty("platform", "dp");

        Http.post("https://api-b2b.backenster.com/b1/api/v3/translate")
                .header("accept", "application/json, text/javascript, */*; q=0.01")
                .header("accept-language", "ru,en;q=0.9")
                .header("authorization", "Bearer a_25rccaCYcBC9ARqMODx2BV2M0wNZgDCEl3jryYSgYZtF1a702PVi4sxqi2AmZWyCcw4x209VXnCYwesx")
                .header("content-type", "application/json")
                .header("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"90\", \"Yandex\";v=\"90\"")
                .header("sec-ch-ua-mobile", "?0")
                .header("sec-fetch-dest", "empty")
                .header("sec-fetch-mode", "cors")
                .header("sec-fetch-site", "cross-site")
                .content(json.toString())
                .error(e -> cons.get(""))
                .submit(response -> cons.get(gson.fromJson(response.getResultAsString(), JsonObject.class).get("result").getAsString()));
    }

    public static void loadLanguages() {
        Http.get("https://api-b2b.backenster.com/b1/api/v3/getLanguages?platform=dp")
                .header("accept", "application/json, text/javascript, */*; q=0.01")
                .header("accept-language", "ru,en;q=0.9")
                .header("authorization", "Bearer a_25rccaCYcBC9ARqMODx2BV2M0wNZgDCEl3jryYSgYZtF1a702PVi4sxqi2AmZWyCcw4x209VXnCYwesx")
                .header("content-type", "application/json")
                .header("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"90\", \"Yandex\";v=\"90\"")
                .header("sec-ch-ua-mobile", "?0")
                .header("sec-fetch-dest", "empty")
                .header("sec-fetch-mode", "cors")
                .header("sec-fetch-site", "cross-site")
                .header("if-none-match", "W/\"aec6-7FjvQqCRl/1E+dvnCAlbAedDteg\"")
                .submit(response -> {
                    JsonArray languages = gson.fromJson(response.getResultAsString(), JsonObject.class).get("result").getAsJsonArray();
                    for (JsonElement element : languages) {
                        JsonObject language = element.getAsJsonObject();
                        codeLanguages.put(language.get("code_alpha_1").getAsString(), language.get("full_code").getAsString());
                    }
                });
    }
}
