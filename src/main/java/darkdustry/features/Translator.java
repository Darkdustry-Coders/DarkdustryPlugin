package darkdustry.features;

import arc.func.Cons;
import arc.struct.StringMap;
import arc.util.*;
import arc.util.serialization.Jval;
import darkdustry.utils.Find;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import darkdustry.DarkdustryPlugin;

import static arc.util.Strings.*;
import static darkdustry.components.Bundle.bundled;
import static mindustry.Vars.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Database.*;

public class Translator {

    public static int left = 500000;

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

        DarkdustryPlugin.info("Loaded @ languages for translator.", translatorLanguages.size);
    }

    public static void translate(String to, String text, Cons<String> result, Cons<Throwable> error) {
        Http.post(translatorApiUrl, "to=" + to + "&text=" + text)
                .header("content-type", "application/x-www-form-urlencoded")
                .header("X-RapidAPI-Key", config.translatorApiKey)
                .header("X-RapidAPI-Host", translatorApiHost)
                .error(error)
                .submit(response -> {
                    left = parseInt(response.getHeader("x-ratelimit-requests-remaining"));
                    result.get(Jval.read(response.getResultAsString()).getString("translated_text"));
                });
    }

    public static void translate(Player author, String text) {
        var cache = new StringMap();
        String message = netServer.chatFormatter.format(author, text);

        Groups.player.each(player -> player != author, player -> {
            String language = getPlayerData(player).language;
            if (language.equals("off") || language.equals(Find.language(author.locale))) {
                player.sendMessage(message, author, text);
                return;
            }

            if (cache.containsKey(language)) {
                player.sendMessage(cache.get(language), author, text);
                return;
            }

            translate(language, stripColors(text), result -> {
                cache.put(language, message + " [white]([lightgray]" + result + "[])");
                player.sendMessage(cache.get(language), author, text);
            }, throwable -> bundled(player, left == 0 ? "translator.limit" : "translator.error", message, left));
        });
    }
}
