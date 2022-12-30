package darkdustry.components;

import arc.func.Cons;
import arc.struct.StringMap;
import arc.util.*;
import arc.util.serialization.JsonReader;
import mindustry.gen.*;

import static darkdustry.PluginVars.translationApiUrl;
import static darkdustry.components.Database.getPlayersData;
import static mindustry.Vars.netServer;

public class Translator {

    public static void translate(String text, String from, String to, Cons<String> result, Runnable error) {
        Http.post(translationApiUrl, "tl=" + to + "&sl=" + from + "&q=" + Strings.encode(text))
                .error(throwable -> error.run())
                .submit(response -> result.get(new JsonReader().parse(response.getResultAsString()).get(0).get(0).asString()));
    }

    public static void translate(Player author, String text) {
        var cache = new StringMap();
        var message = netServer.chatFormatter.format(author, text);

        getPlayersData(Groups.player, (player, data) -> {
            if (player == author) return;

            if (data.language.equals("off")) {
                player.sendMessage(message, author, text);
                return;
            }

            if (cache.containsKey(data.language)) {
                player.sendMessage(cache.get(data.language), author, text);
            } else translate(text, "auto", data.language, result -> {
                cache.put(data.language, message + " [white]([lightgray]" + result + "[])");
                player.sendMessage(cache.get(data.language), author, text);
            }, () -> player.sendMessage(message, author, text));
        }).subscribe();
    }
}