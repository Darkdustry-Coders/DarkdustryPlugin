package pandorum.events.filters;

import arc.struct.ObjectMap;
import arc.util.Log;
import arc.util.Strings;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.comp.Translator;
import pandorum.models.PlayerModel;

import static mindustry.Vars.netServer;
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

            String language = playerModel.locale.equals("auto") ? player.locale : playerModel.locale;
            if (cache.containsKey(language)) {
                player.sendMessage(formatTranslated(formatted, text, cache.get(language)), author, text);
                return;
            }

            Translator.translate(Strings.stripColors(text), language, translated -> {
                player.sendMessage(formatTranslated(formatted, text, translated), author, text);
                cache.put(language, translated);
            });
        }));

        text("**@**: @", Strings.stripColors(author.name), text);
        return null;
    }

    private static String formatTranslated(String formatted, String text, String translatedText) {
        return translatedText.equalsIgnoreCase(text) || translatedText.isBlank() ? formatted : formatted + " [white]([lightgray]" + translatedText + "[white])";
    }
}
