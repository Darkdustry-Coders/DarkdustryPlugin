package pandorum.listeners.filters;

import arc.struct.ObjectMap;
import arc.util.Log;
import arc.util.Strings;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration.ChatFilter;
import pandorum.data.PlayerData;
import pandorum.features.Translator;
import pandorum.util.Utils;

import static mindustry.Vars.netServer;
import static pandorum.PluginVars.*;
import static pandorum.data.Database.getPlayerData;
import static pandorum.discord.Bot.text;
import static pandorum.util.Search.findTranslatorLocale;

public class ChatManager implements ChatFilter {

    private static String formatTranslated(String formatted, String translatedText) {
        return translatedText.isBlank() ? formatted : formatted + " [white]([lightgray]" + translatedText + "[white])";
    }

    public String filter(Player author, String text) {
        String formatted = netServer.chatFormatter.format(author, text);
        ObjectMap<String, String> cache = new ObjectMap<>();

        Log.info("&fi@: @", "&lc" + author.name, "&lw" + text);
        author.sendMessage(formatted, author, text);

        Groups.player.each(player -> player != author, player -> {
            PlayerData data = getPlayerData(player.uuid());
            if (data.locale.equals("off")) {
                player.sendMessage(formatted, author, text);
                return;
            }

            String locale = data.locale.equals("auto") ? Utils.notNullElse(findTranslatorLocale(player.locale), defaultLocale) : data.locale;
            if (cache.containsKey(locale)) {
                player.sendMessage(formatTranslated(formatted, cache.get(locale)), author, text);
                return;
            }

            Translator.translate(Utils.stripAll(text), codeLanguages.get(locale, defaultTranslatorLocale), translated -> {
                player.sendMessage(formatTranslated(formatted, translated), author, text);
                cache.put(locale, translated);
            });
        });

        text("**@**: @", Strings.stripColors(author.name), text);
        return null;
    }
}
