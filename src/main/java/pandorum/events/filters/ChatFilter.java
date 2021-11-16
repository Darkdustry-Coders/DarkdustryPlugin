package pandorum.events.filters;

import arc.struct.StringMap;
import arc.util.Log;
import arc.util.Strings;
import com.mongodb.BasicDBObject;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.PandorumPlugin;
import pandorum.discord.BotHandler;
import pandorum.models.PlayerModel;

import static mindustry.Vars.netServer;

public class ChatFilter {
    public static String filter(final Player author, final String text) {
        String formatted = netServer.chatFormatter.format(author, text);
        StringMap cache = new StringMap();

        author.sendMessage(formatted, author, text);
        Log.info("&fi@: @", "&lc" + author.name, "&lw" + text);

        Groups.player.each(player -> !player.equals(author), player -> PlayerModel.find(new BasicDBObject("UUID", player.uuid()), playerInfo -> {
            if (playerInfo.locale.equals("off")) {
                player.sendMessage(formatted, author, text);
                return;
            }

            String language = playerInfo.locale.equals("auto") ? player.locale() : playerInfo.locale;
            if (cache.containsKey(language)) {
                player.sendMessage(Strings.format("@ [white]([gray]@[white])", formatted, cache.get(language)), author, text);
                return;
            }

            PandorumPlugin.translator.translate(text, language, translated -> {
                if ((!translated.isNull("err") && translated.optString("err").equals("")) || translated.optString("result", text).equalsIgnoreCase(text) || translated.optString("result", text).isBlank()) {
                    player.sendMessage(formatted, author, text);
                    cache.put(language, text);
                    return;
                }

                String translatedText = translated.optString("result", text);
                player.sendMessage(Strings.format("@ [white]([gray]@[white])", formatted, translatedText), author, text);
                cache.put(language, translatedText);
            });
        }));

        BotHandler.text(BotHandler.botChannel, "**@**: @", Strings.stripColors(author.name), Strings.stripColors(text).replaceAll("https?://|@", " "));
        return null;
    }
}
