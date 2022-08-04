package rewrite.listeners;

import arc.util.Log;
import mindustry.gen.Player;
import mindustry.net.Administration.ActionType;
import mindustry.net.Administration.PlayerAction;
import rewrite.discord.Bot;
import rewrite.features.Translator;
import rewrite.features.history.History;
import rewrite.features.history.RotateEntry;

import static arc.util.Strings.*;
import static mindustry.Vars.*;

public class Filters {
    
    public static boolean action(PlayerAction action) {
        if (History.enabled() && action.type == ActionType.rotate) History.put(new RotateEntry(action), action.tile);
        return true;
    }

    public static String chat(Player author, String text) {
        Log.info("&fi@: @", "&lc" + author.name, "&lw" + text);

        author.sendMessage(netServer.chatFormatter.format(author, text), author, text);
        Translator.translate(author, text);

        Bot.sendMessage(Bot.botChannel, "@ » @", stripColors(author.name), stripColors(text));
        return null;
    }
}
