package pandorum.listeners.filters;

import arc.util.Log;
import arc.util.Strings;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration.ChatFilter;
import pandorum.data.PlayerData;
import pandorum.features.Translator;
import pandorum.util.StringUtils;
import pandorum.util.Utils;

import static mindustry.Vars.netServer;
import static pandorum.PluginVars.defaultLocale;
import static pandorum.data.Database.getPlayerData;
import static pandorum.discord.Bot.text;
import static pandorum.util.Search.findTranslatorLocale;

public class ChatManager implements ChatFilter {

    public String filter(Player author, String text) {
        String formatted = netServer.chatFormatter.format(author, text);

        Log.info("&fi@: @", "&lc" + author.name, "&lw" + text);
        author.sendMessage(formatted, author, text);

        Groups.player.each(player -> player != author, player -> {
            PlayerData data = getPlayerData(player.uuid());
            if (data.locale.equals("off")) {
                player.sendMessage(formatted, author, text);
                return;
            }

            String locale = data.locale.equals("auto") ? Utils.notNullElse(findTranslatorLocale(player.locale), defaultLocale) : data.locale;

            Translator.translate(StringUtils.stripAll(text), locale, translated -> player.sendMessage(formatted + (translated.isBlank() ? "" : " [white]([lightgray]" + translated + "[white])"), author, text));
        });

        text("**@**: @", Strings.stripColors(author.name), text);
        return null;
    }
}
