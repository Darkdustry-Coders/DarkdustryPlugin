package pandorum.events;

import arc.util.Strings;
import arc.util.Log;
import mindustry.gen.Player;
import mindustry.gen.Groups;

import pandorum.comp.DiscordWebhookManager;
import pandorum.comp.Translator;
import pandorum.PandorumPlugin;

import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

public class ChatFilter {
    public static String call(final Player author, final String text) {
        author.sendMessage(text, author);
        Log.info("&fi&lc@: &lw@", author.name, text);

        Map<String, String> translationsCache = new HashMap<>();
        Groups.player.each(player -> !player.equals(author), player -> {

            String setting;
            //setting = получаем инфу из БД.
            if (setting.equals("off")) {
                player.sendMessage(text, author);
                return;
            }
            String language = setting.equals("auto") ? player.locale() : setting;

            if (translationsCache.containsKey(language)) {
                player.sendMessage(text + " [white]([gray]" + translationsCache.get(language) + "[white])", author);
                return;
            }

            String translatedText = null;
            try {
                JSONObject translatorRes = Translator.translate(text, language);
                if (!translatorRes.isNull("err") && translatorRes.optString("err").equals("")) {
                    player.sendMessage(text, author);
                    translationsCache.put(language, text);
                    Log.info("Ошибка перевода: ", translatorRes.get("err"));
                    return;
                }
                translatedText = translatorRes.getString("result");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            translationsCache.put(language, translatedText);
            player.sendMessage(text + " [white]([gray]" + translatedText + "[white])", author);
        });
        return null;
    }
}
