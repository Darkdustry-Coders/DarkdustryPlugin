package rewrite.features;

import arc.func.Cons;
import arc.struct.StringMap;
import arc.util.Http;
import arc.util.Strings;
import arc.util.serialization.Jval;
import rewrite.DarkdustryPlugin;

import static rewrite.PluginVars.*;

public class Translator {

    public static int left = 500000;
    public static StringMap cache = new StringMap();

    public static void load() {
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
                "ko", "한국어");

        mindustry2Api.putAll(
                "in_ID", "id",
                "nl_BE", "nl",
                "pt_BR", "pt",
                "pt_PT", "pt",
                "uk_UA", "uk",
                "zh_CN", "zh",
                "zh_TW", "zh");

        DarkdustryPlugin.info("Загружено @ языков для переводчика.", translatorLanguages.size);
    }

    public static void translate(String to, String text, Cons<String> cons) {
        if (!cache.containsKey(to)) Http.post(translatorApiUrl, "to=" + to + "&text=" + text)
                .header("content-type", "application/x-www-form-urlencoded")
                .header("X-RapidAPI-Key", config.translatorApiKey)
                .header("X-RapidAPI-Host", translatorApiHost)
                .error(throwable -> cons.get("Requests left:" + (left = 0)))
                .submit(response -> {
                    left = Strings.parseInt(response.getHeader("x-ratelimit-requests-remaining"));
                    cache.put(to, Jval.read(response.getResultAsString()).getString("translated_text"));
                });
        // кстати не будет работать, т.к. запрос обрабатывается в отдельном потоке и в кэшэ тупо ничего не будет
        cons.get(cache.get(to));
    }
}
