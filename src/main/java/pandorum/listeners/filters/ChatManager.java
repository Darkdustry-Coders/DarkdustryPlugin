package pandorum.listeners.filters;

import arc.util.Log;
import arc.util.Strings;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration.ChatFilter;
import pandorum.features.Translator;

import static mindustry.Vars.netServer;
import static pandorum.discord.Bot.text;
import static pandorum.util.PlayerUtils.getTranslatorLocale;

public class ChatManager implements ChatFilter {

    public String filter(Player author, String text) {
        String formatted = netServer.chatFormatter.format(author, text);

        Log.info("&fi@: @", "&lc" + author.name, "&lw" + text);

        Groups.player.each(player -> {
            String locale = getTranslatorLocale(player);
            if (locale.equals("off")) {
                player.sendMessage(formatted, author, text);
                return;
            }

            Translator.translate(Strings.stripColors(text), locale, translated -> player.sendMessage(formatted + (translated.isBlank() ? "" : " [white]([lightgray]" + translated + "[white])"), author, text));
        });

        text("**@**: @", Strings.stripColors(author.name), Strings.stripColors(text));
        return null;
    }
}
