package pandorum.components;

import arc.func.Cons;
import arc.util.Http;
import arc.util.Log;
import com.google.gson.JsonObject;

import static pandorum.PluginVars.*;

public class Translator {

    public static void loadLanguages() {
        translatorLanguages.putAll(
                "id", "Indonesia",
                "da", "Dansk",
                "de", "Deutsch",
                "et", "Eesti",
                "en", "English",
                "es", "Español",
                "eu", "Euskara",
                "fil", "Filipino",
                "fr", "Français",
                "it", "Italiano",
                "lt", "Lietuvių",
                "hu", "Magyar",
                "nl", "Nederlands",
                "pl", "Polski",
                "pt", "Português",
                "ro", "Română",
                "fi", "Suomi",
                "sv", "Svenska",
                "vi", "Tiếng Việt",
                "tk", "Türkmen dili",
                "tr", "Türkçe",
                "cs", "Čeština",
                "be", "Беларуская",
                "bg", "Български",
                "ru", "Русский",
                "uk", "Українська",
                "th", "ไทย",
                "zh-CN", "简体中文",
                "zh-TW", "正體中文",
                "ja", "日本語",
                "ko", "한국어"
        );

        mindustryLocales2Api.putAll(
                "in_ID", "id",
                "nl_BE", "nl",
                "pt_BR", "pt",
                "pt_PT", "pt",
                "uk_UA", "uk",
                "zh_CN", "zh-CN",
                "zh_TW", "zh-TW",
                "router", "en"
        );

        Log.info("[Darkdustry] Загружено языков для переводчика: @.", translatorLanguages.size);
    }

    public static void translate(String text, String to, Cons<String> cons) {
        JsonObject json = new JsonObject();
        json.addProperty("q", text);
        json.addProperty("source", "auto");
        json.addProperty("target", to);

        Http.post(translatorApiUrl)
                .header("X-RapidAPI-Key", config.translatorApiKey)
                .content(json.toString())
                .error(throwable -> cons.get(""))
                .submit(response -> {
                    String translatedText = gson.fromJson(response.getResultAsString(), JsonObject.class).getAsJsonObject("data").getAsJsonObject("translations").get("translatedText").getAsString();
                    cons.get(translatedText.equalsIgnoreCase(text) ? "" : translatedText);
                });
    }
}
