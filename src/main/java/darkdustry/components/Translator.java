package darkdustry.components;

import arc.func.*;
import arc.util.Http;
import arc.util.serialization.JsonReader;
import darkdustry.features.menus.MenuHandler.Language;
import mindustry.gen.*;
import useful.Bundle;

import static arc.util.Strings.*;
import static darkdustry.PluginVars.*;

public class Translator {

    public static void translate(String text, String from, String to, Cons<String> result) {
        Http.post(translationApiUrl, "tl=" + to + "&sl=" + from + "&q=" + encode(text))
                .error(throwable -> result.get(""))
                .submit(response -> result.get(new JsonReader().parse(response.getResultAsString()).child().child().asString()));
    }

    public static void translate(Player from, String text) {
        translate(player -> true, from, text, (player, result) -> player.sendUnformatted(from, result));
    }

    public static void translate(Boolf<Player> filter, Player from, String text, String key, Object... values) {
        translate(filter, from, text, (player, result) -> Bundle.sendFrom(player, from, result, key, values));
    }

    public static void translate(Boolf<Player> filter, Player from, String text, Cons2<Player, String> result) {
        Groups.player.each(filter, player -> {
            var data = Cache.get(player);

            if (player == from || data.language == Language.off) {
                result.get(player, text);
                return;
            }

            translate(text, "auto", data.language.code, translated -> result.get(player, translated.isEmpty() ? text : text + " [white]([lightgray]" + translated + "[])"));
        });
    }
}