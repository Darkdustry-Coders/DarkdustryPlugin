package pandorum.events;

import arc.util.Log;
import arc.util.Strings;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import org.bson.Document;
import pandorum.PandorumPlugin;
import pandorum.comp.DiscordWebhookManager;
import pandorum.comp.Translator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
                if (translationsCache.get(language).equals("err")) {
                    player.sendMessage(text, author);
                    return;
                }
                player.sendMessage(text + " [white]([gray]" + translationsCache.get(language) + "[white])", author);
                return;
            }

            try {
                Translator.translate(text, language, (translatorRes) -> {
                    if (!translatorRes.isNull("err") && translatorRes.optString("err").equals("")) {
                        player.sendMessage(text, author);
                        translationsCache.put(language, text);
                        Log.info("Ошибка перевода: ", translatorRes.get("err"));
                        return;
                    }

                    String translatedText = translatorRes.optString("result", "err");
                    translationsCache.put(language, translatedText);
                    if (translatedText.equals("err")) {
                        player.sendMessage(text, author);
                        return;
                    }
                    player.sendMessage(text + " [white]([gray]" + translatedText + "[white])", author);
                });
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        DiscordWebhookManager.client.send(String.format("**[%s]:** %s", Strings.stripColors(author.name), text.replaceAll("https?://|@", " ")));
        return null;
    }
}
