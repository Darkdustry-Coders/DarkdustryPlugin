package pandorum.features;

import arc.func.Cons;
import arc.util.Http;
import arc.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static pandorum.PluginVars.translatorLocales;
import static pandorum.PluginVars.gson;

public class Translator {

    public static void translate(String text, String to, Cons<String> cons) {
        JsonObject json = new JsonObject();
        json.addProperty("q", text);
        json.addProperty("source", "auto");
        json.addProperty("target", to);

        Http.post("https://libretranslate.de/translate")
                .header("user-agent", "darkdustry")
                .header("content-type", "application/json")
                .content(json.toString())
                .error(e -> cons.get(""))
                .submit(response -> cons.get(gson.fromJson(response.getResultAsString(), JsonObject.class).get("translatedText").getAsString()));
    }

    public static void translateGoogle(String text, String to, Cons<String> cons) {
        Http.get("https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=" + to + "&dt=t&q=" + URLEncoder.encode(text, StandardCharsets.UTF_8))
                .header("user-agent", "darkdustry")
                .error(e -> cons.get(""))
                .submit(response -> {
                    JsonArray array = gson.fromJson(response.getResultAsString(), JsonArray.class);
                    String translated = array.get(0).getAsJsonArray().get(0).getAsJsonArray().get(0).getAsString();

                    Log.info(array);
                    Log.info(translated);

                    cons.get(translated);
                });
    }

    public static void loadLanguages() {
        Http.get("https://libretranslate.de/languages")
                .header("user-agent", "darkdustry")
                .header("content-type", "application/json")
                .submit(response -> {
                    JsonArray languages = gson.fromJson(response.getResultAsString(), JsonArray.class).getAsJsonArray();
                    for (JsonElement element : languages) {
                        JsonObject language = element.getAsJsonObject();
                        translatorLocales.put(language.get("code").getAsString(), language.get("name").getAsString());
                    }
                });
    }
}
