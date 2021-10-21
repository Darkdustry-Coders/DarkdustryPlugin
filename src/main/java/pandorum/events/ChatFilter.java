package pandorum.events;

import arc.util.Log;
import arc.util.Strings;
import com.mongodb.BasicDBObject;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.comp.Translator;
import pandorum.discord.BotHandler;
import pandorum.models.PlayerModel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static mindustry.Vars.netServer;

public class ChatFilter {
    public static String filter(final Player author, final String text) {
        author.sendMessage(netServer.chatFormatter.format(author, text), author, text);
        Log.info("&fi@: @", "&lc" + author.name, "&lw" + text);
        Map<String, String> translationsCache = new HashMap<>();

        Groups.player.each(player -> !player.equals(author), player -> PlayerModel.find(new BasicDBObject("UUID", player.uuid()), playerInfo -> {

            if (playerInfo.locale.equals("off")) {
                player.sendMessage(netServer.chatFormatter.format(author, text), author, text);
                return;
            }

            String language = Objects.equals(playerInfo.locale, "auto") ? player.locale() : playerInfo.locale;
            if (translationsCache.containsKey(language)) {
                player.sendMessage(netServer.chatFormatter.format(author, text) + (translationsCache.get(language).equalsIgnoreCase(text) ? "" : " [white]([gray]" + translationsCache.get(language) + "[white])"), author, text);
                return;
            }

            try {
                Translator.translate(text, language, (translated) -> {
                    if (!translated.isNull("err") && translated.optString("err").equals("")) {
                        player.sendMessage(netServer.chatFormatter.format(author, text), author, text);
                        translationsCache.put(language, text);
                        return;
                    }

                    String translatedText = translated.optString("result", text);
                    if (translatedText.equalsIgnoreCase(text) || translatedText.isBlank()) {
                        translationsCache.put(language, text);
                        player.sendMessage(netServer.chatFormatter.format(author, text), author, text);
                        return;
                    }
                    translationsCache.put(language, translatedText);
                    player.sendMessage(netServer.chatFormatter.format(author, text) + (translationsCache.get(language).equalsIgnoreCase(text) ? "" : " [white]([gray]" + translationsCache.get(language) + "[white])"), author, text);
                });
            } catch (IOException | InterruptedException ignored) {}
        }));

        BotHandler.text("**@**: @", Strings.stripColors(author.name), text.replaceAll("https?://|@", " "));
        return null;
    }
}
