package darkdustry.components;

import arc.func.Cons;
import arc.struct.StringMap;
import arc.util.Http;
import arc.util.Strings;
import arc.util.serialization.JsonReader;
import darkdustry.features.menus.MenuHandler.Language;
import mindustry.gen.Groups;
import mindustry.gen.Player;

import static darkdustry.PluginVars.translationApiUrl;
import static mindustry.Vars.netServer;

public class Translator {

    public static void translate(String text, String from, String to, Cons<String> result) {
        Http.post(translationApiUrl, "tl=" + to + "&sl=" + from + "&q=" + Strings.encode(text))
                .error(throwable -> result.get(""))
                .submit(response -> result.get(new JsonReader().parse(response.getResultAsString()).get(0).get(0).asString()));
    }

    public static void translate(Player author, String text) {
        var cache = new StringMap();
        var message = netServer.chatFormatter.format(author, text);

        Groups.player.each(player -> player != author, player -> {
            var data = Cache.get(player);

            if (data.language == Language.off) {
                player.sendMessage(message, author, text);
                return;
            }

            if (cache.containsKey(data.language.code)) {
                player.sendMessage(cache.get(data.language.code), author, text);
                return;
            }

            translate(text, "auto", data.language.code, translated -> {
                var result = translated.isEmpty() ? message : message + " [white]([lightgray]" + translated + "[])";

                cache.put(data.language.code, result);
                player.sendMessage(result, author, text);
            });
        });
    }
}