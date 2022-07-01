package pandorum.listeners.filters;

import arc.util.Log;
import arc.util.Strings;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.net.Administration.ChatFilter;

import static mindustry.Vars.netServer;
import static pandorum.discord.Bot.text;

public class ChatManager implements ChatFilter {

    public String filter(Player author, String text) {
        Log.info("&fi@: @", "&lc" + author.name, "&lw" + text);
        Call.sendMessage(netServer.chatFormatter.format(author, text), text, author);
        text("**@**: @", Strings.stripColors(author.name), text);

        return null;
    }
}
