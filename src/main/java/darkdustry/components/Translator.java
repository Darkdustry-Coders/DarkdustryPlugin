package darkdustry.components;

import arc.func.Cons;
import arc.struct.StringMap;
import arc.util.*;
import arc.util.serialization.JsonReader;
import darkdustry.features.menus.MenuHandler.Language;
import mindustry.gen.*;

import static darkdustry.PluginVars.*;
import static mindustry.Vars.*;

public class Translator {

    public static void translate(String text, String from, String to, Cons<String> result) {
        Http.post(translationApiUrl, "tl=" + to + "&sl=" + from + "&q=" + Strings.encode(text))
                .error(throwable -> result.get(""))
                .submit(response -> result.get(new JsonReader().parse(response.getResultAsString()).get(0).get(0).asString()));
    }

    public static void translate(Player author, String text) {
        var cache = new StringMap();

        Groups.player.each(player -> player != author, player -> {
            var data = Cache.get(player);

            if (data.language == Language.off) {
                player.sendUnformatted(author, text);
                return;
            }

            if (cache.containsKey(data.language.code)) {
                player.sendUnformatted(author, cache.get(data.language.code));
                return;
            }

            translate(text, "auto", data.language.code, translated -> {
                var result = translated.isEmpty() ? text : text + " [white]([lightgray]" + translated + "[])";

                cache.put(data.language.code, result);
                player.sendUnformatted(author, result);
            });
        });
    }
}