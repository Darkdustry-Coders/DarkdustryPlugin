package pandorum.components;

import arc.func.Cons;
import arc.struct.StringMap;
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
                "zh", "简体中文",
                "ja", "日本語",
                "ko", "한국어"
        );

        mindustryLocales2Api.putAll(
                "in_ID", "id",
                "nl_BE", "nl",
                "pt_BR", "pt",
                "pt_PT", "pt",
                "uk_UA", "uk",
                "zh_CN", "zh",
                "zh_TW", "zh"
        );

        Log.info("[Darkdustry] Загружено языков для переводчика: @.", translatorLanguages.size);
    }

    public static void translate(String to, String text, Cons<String> cons) {
        Http.post(translatorApiUrl, "to=" + to + "&text=" + text)
                .header("content-type", "application/x-www-form-urlencoded")
                .header("X-RapidAPI-Key", config.translatorApiKey)
                .header("X-RapidAPI-Host", translatorApiHost)
                .error(throwable -> cons.get(""))
                .submit(response -> {
                    String translatedText = gson.fromJson(response.getResultAsString(), JsonObject.class).get("translated_text").getAsString();
                    cons.get(translatedText);
                });
    }
}
