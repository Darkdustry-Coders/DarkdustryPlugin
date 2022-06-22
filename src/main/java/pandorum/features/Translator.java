package pandorum.features;

import arc.func.Cons;
import arc.util.Http;
import arc.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import static pandorum.PluginVars.*;

public class Translator {

    public static void translate(String text, String to, Cons<String> cons) {
        JsonObject json = new JsonObject();
        json.addProperty("to", to);
        json.addProperty("text", text);
        json.addProperty("platform", "api");
        json.addProperty("enableTransliteration", true);

        Http.post("https://api-b2b.backenster.com/b1/api/v3/translate")
                .header("authorization", translatorApiKey)
                .header("content-type", "application/json")
                .content(json.toString())
                .error(e -> cons.get(""))
                .submit(response -> cons.get(gson.fromJson(response.getResultAsString(), JsonObject.class).get("result").getAsString()));
    }

    public static void loadLanguages() {
        Http.get("https://api-b2b.backenster.com/b1/api/v3/getLanguages?platform=api")
                .header("authorization", translatorApiKey)
                .header("content-type", "application/json")
                .error(e -> Log.err("[Darkdustry] Не удалось загрузить языки для переводчика чата", e))
                .submit(response -> {
                    JsonArray languages = gson.fromJson(response.getResultAsString(), JsonObject.class).get("result").getAsJsonArray();
                    for (JsonElement element : languages) {
                        JsonObject language = element.getAsJsonObject();
                        translatorLanguages.put(language.get("code_alpha_1").getAsString(), language.get("full_code").getAsString());
                        //Log.info("@ @ @", language.get("code_alpha_1").getAsString(), language.get("full_code").getAsString(), language.get("englishName").getAsString());
                    }

                    Log.info("[Darkdustry] Загружено @ языков для перевода.", translatorLanguages.size);
                });
    }
}
