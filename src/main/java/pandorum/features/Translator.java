package pandorum.features;

import arc.func.Cons;
import arc.util.Http;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import static pandorum.PluginVars.codeLanguages;
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

    public static void loadLanguages() {
        Http.get("https://libretranslate.de/languages")
                .header("user-agent", "darkdustry")
                .header("content-type", "application/json")
                .submit(response -> {
                    JsonArray languages = gson.fromJson(response.getResultAsString(), JsonObject.class).getAsJsonArray();
                    for (JsonElement element : languages) {
                        JsonObject language = element.getAsJsonObject();
                        codeLanguages.put(language.get("code").getAsString(), language.get("name").getAsString());
                    }
                });
    }
}
