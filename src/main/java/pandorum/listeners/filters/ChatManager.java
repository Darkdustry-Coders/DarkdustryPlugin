package pandorum.listeners.filters;

import arc.util.Log;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration.ChatFilter;
import pandorum.components.Translator;
import pandorum.data.PlayerData;
import pandorum.discord.Bot;

import static mindustry.Vars.netServer;
import static pandorum.data.Database.getPlayerData;
import static pandorum.discord.Bot.botChannel;
import static pandorum.util.StringUtils.stripAll;

public class ChatManager implements ChatFilter {

    public String filter(Player author, String text) {
        Log.info("&fi@: @", "&lc" + author.name, "&lw" + text);
        author.sendMessage(netServer.chatFormatter.format(author, text), author, text);

        Groups.player.each(player -> player != author, player -> {
            PlayerData data = getPlayerData(player.uuid());
            if (data.language.equals("off")) {
                player.sendMessage(netServer.chatFormatter.format(author, text), author, text);
                return;
            }

            Translator.translate(stripAll(text), data.language, translated -> player.sendMessage(netServer.chatFormatter.format(author, text) + (translated.isBlank() ? "" : " [white]([lightgray]" + translated + "[white])"), author, text));
        });

        Bot.sendMessage(botChannel, "@ » @", stripAll(author.name), stripAll(text));
        return null;
    }
}
