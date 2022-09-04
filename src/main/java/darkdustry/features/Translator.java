package darkdustry.features;

import arc.func.Cons;
import arc.struct.*;
import arc.util.Http;
import arc.util.serialization.Jval;
import darkdustry.DarkdustryPlugin;
import darkdustry.utils.Find;
import mindustry.gen.*;

import static arc.util.Strings.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.bundled;
import static darkdustry.components.MongoDB.getPlayersData;
import static mindustry.Vars.netServer;

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
                "ko", "한국어"
        );

        mindustry2Api.putAll(
                "in_ID", "id",
                "nl_BE", "nl",
                "pt_BR", "pt",
                "pt_PT", "pt",
                "uk_UA", "uk",
                "zh_CN", "zh",
                "zh_TW", "zh"
        );

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

        getPlayersData(Groups.player.copy(new Seq<>()).map(Player::uuid)).doOnNext(data -> {
            Player player = Find.playerByUuid(data.uuid);
            if (player == null || player == author) return;

            if (data.language.equals("off") || data.language.equals(Find.language(author.locale))) {
                player.sendMessage(message, author, text);
                return;
            }

            if (cache.containsKey(data.language)) {
                player.sendMessage(cache.get(data.language), author, text);
            } else translate(data.language, stripColors(text), result -> {
                cache.put(data.language, message + " [white]([lightgray]" + result + "[])");
                player.sendMessage(cache.get(data.language), author, text);
            }, e -> bundled(player, left == 0 ? "translator.limit" : "translator.error", message, left));
        }).subscribe();
    }
}
