package pandorum.events.filters;

import arc.struct.StringMap;
import arc.util.Log;
import arc.util.Strings;
import com.mongodb.BasicDBObject;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.comp.Translator;
import pandorum.discord.BotHandler;
import pandorum.models.PlayerModel;

import java.io.IOException;

import static mindustry.Vars.netServer;

public class ChatFilter {
    public static String filter(final Player author, final String text) {
        author.sendMessage(netServer.chatFormatter.format(author, text), author, text);
        Log.info("&fi@: @", "&lc" + author.name, "&lw" + text);
        StringMap translationsCache = new StringMap();

        Groups.player.each(player -> !player.equals(author), player -> PlayerModel.find(
            new BasicDBObject("UUID", player.uuid()), playerInfo -> {
                if (playerInfo.locale.equals("off")) {
                    player.sendMessage(netServer.chatFormatter.format(author, text), author, text);
                    return;
                }

                String language = playerInfo.locale.equals("auto") ? player.locale() : playerInfo.locale;
                if (translationsCache.containsKey(language)) {
                    player.sendMessage(netServer.chatFormatter.format(author, text) + (translationsCache.get(language).equalsIgnoreCase(text) ? "" : " [white]([gray]" + translationsCache.get(language) + "[white])"), author, text);
                    return;
                }

                try {
                    Translator.translate(text, language, translated -> {
                        if ((!translated.isNull("err") && translated.optString("err").equals("")) || translated.optString("result", text).equalsIgnoreCase(text) || translated.optString("result", text).isBlank()) {
                            player.sendMessage(netServer.chatFormatter.format(author, text), author, text);
                            translationsCache.put(language, text);
                            return;
                        }

                        String translatedText = translated.optString("result", text);
                        player.sendMessage(netServer.chatFormatter.format(author, text) + " [white]([gray]" + translatedText + "[white])", author, text);
                        translationsCache.put(language, translatedText);
                    });
                } catch (IOException | InterruptedException ignored) {}
            }
        ));

        BotHandler.text("**@**: @", Strings.stripColors(author.name), text.replaceAll("https?://|@", " "));
        return null;
    }
}
