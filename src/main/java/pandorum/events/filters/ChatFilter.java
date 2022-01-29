package pandorum.events.filters;

import arc.struct.ObjectMap;
import arc.util.Log;
import arc.util.Strings;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.comp.Translator;
import pandorum.models.PlayerModel;
import pandorum.util.Utils;

import static mindustry.Vars.netServer;
import static pandorum.PluginVars.codeLanguages;
import static pandorum.PluginVars.defaultLocale;
import static pandorum.discord.Bot.text;

public class ChatFilter {

    public static String filter(final Player author, final String text) {
        String formatted = netServer.chatFormatter.format(author, text);
        ObjectMap<String, String> cache = new ObjectMap<>();

        Log.info("&fi@: @", "&lc" + author.name, "&lw" + text);
        author.sendMessage(formatted, author, text);

        Groups.player.each(player -> player != author, player -> PlayerModel.find(player, playerModel -> {
            if (playerModel.locale.equals("off")) {
                player.sendMessage(formatted, author, text);
                return;
            }

            String locale = playerModel.locale.equals("auto") ? Utils.notNullElse(codeLanguages.keys().toSeq().find(key -> player.locale.equalsIgnoreCase(key) || player.locale.startsWith(key)), defaultLocale) : playerModel.locale;
            if (cache.containsKey(locale)) {
                player.sendMessage(formatTranslated(formatted, text, cache.get(locale)), author, text);
                return;
            }

            Translator.translate(Strings.stripColors(text), locale, translated -> {
                player.sendMessage(formatTranslated(formatted, text, translated), author, text);
                cache.put(locale, translated);
            });
        }));

        text("**@**: @", Strings.stripColors(author.name), text);
        return null;
    }

    private static String formatTranslated(String formatted, String text, String translatedText) {
        return translatedText.isBlank() ? formatted : formatted + " [white]([lightgray]" + translatedText + "[white])";
    }
}
