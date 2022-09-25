package darkdustry.components;

import arc.func.Cons;
import arc.struct.*;
import arc.util.Http;
import arc.util.serialization.Jval;
import darkdustry.DarkdustryPlugin;
import darkdustry.utils.Find;
import mindustry.gen.*;

import static arc.util.Strings.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.MongoDB.getPlayersData;
import static mindustry.Vars.netServer;

public class Translator {

    public static void load() {
        translatorLanguages.putAll(
                "ca", "Català",
                "id", "Bahasa Indonesia",
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
                "sr", "Српски",
                "uk_UA", "Українська",
                "th", "ไทย",
                "zh", "简体中文",
                "ja", "日本語",
                "ko", "한국어"
        );

        DarkdustryPlugin.info("Loaded @ translator languages.", translatorLanguages.size);
    }

    public static void translate(String text, String from, String to, Cons<String> result, Cons<Throwable> error) {
        Http.post(translatorApiUrl)
                .content("tl=" + to + "&sl=" + from + "&q=" + encode(text))
                .error(error)
                .submit(response -> result.get(Jval.read(response.getResultAsString()).asArray().get(0).asArray().get(0).asString()));
    }

    public static void translate(Player author, String text) {
        var cache = new StringMap();
        var message = netServer.chatFormatter.format(author, text);

        getPlayersData(Groups.player.copy(new Seq<>()).map(Player::uuid)).doOnNext(data -> {
            var player = Find.playerByUuid(data.uuid);
            if (player == null || player == author) return;

            if (data.language.equals("off") || data.language.equals(author.locale)) {
                player.sendMessage(message, author, text);
                return;
            }

            if (cache.containsKey(data.language)) {
                player.sendMessage(cache.get(data.language), author, text);
            } else translate(stripColors(text), "auto", data.language, result -> {
                cache.put(data.language, message + " [white]([lightgray]" + result + "[])");
                player.sendMessage(cache.get(data.language), author, text);
            }, e -> player.sendMessage(message, author, text));
        }).subscribe();
    }
}
