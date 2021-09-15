package pandorum.events;

import arc.util.Strings;
import arc.util.Log;
import mindustry.gen.Player;
import mindustry.gen.Groups;

import pandorum.comp.DiscordWebhookManager;
import pandorum.comp.Translator;
import pandorum.PandorumPlugin;
import pandorum.models.PlayerInfo;

import org.json.JSONObject;
import org.bson.Document;

import java.io.IOException;
import java.util.*;

public class ChatFilter {
    public static String call(final Player author, final String text) {
        author.sendMessage(text, author);
        Log.info("&fi&lc@: &lw@", author.name, text);
        Map<String, String> translationsCache = new HashMap<>();
        Groups.player.each(player -> !player.equals(author), player -> {

            Document playerInfo = PandorumPlugin.playersInfo.find((playerInfo2) -> playerInfo2.getString("uuid").equals(player.uuid()));
            if (playerInfo == null) {
                playerInfo = PandorumPlugin.playerInfoSchema.create(player.uuid(), true, false, "off");
                PandorumPlugin.playersInfo.add(playerInfo);
            }
            String locale = playerInfo.getString("locale");

            if (locale == null || locale.equals("off")) { 
                player.sendMessage(text, author);
                return;
            }

            String language = locale.equals("auto") ? player.locale() : locale;

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

        DiscordWebhookManager.client.send(String.format("**[%s]:** %s", Strings.stripColors(player.name), text.replaceAll("https?://|@", " ")));
        return null;
    }
}
