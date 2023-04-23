package darkdustry.listeners;

import arc.util.Log;
import darkdustry.components.Translator;
import darkdustry.features.history.*;
import mindustry.gen.Player;
import mindustry.net.Administration.PlayerAction;

import static darkdustry.PluginVars.*;
import static darkdustry.discord.Bot.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;
import static mindustry.net.Administration.ActionType.*;

public class Filters {

    public static boolean action(PlayerAction action) {
        if (History.enabled() && action.type == rotate)
            History.put(new RotateEntry(action), action.tile);

        return true;
    }

    public static String chat(Player author, String text) {
        int sign = voteChoice(text);
        if (sign != 0 && vote != null) {
            if (!alreadyVoted(author, vote)) vote.vote(author, sign);
            return null;
        }

        Log.info("&fi@: @", "&lc" + author.plainName(), "&lw" + text);

        author.sendMessage(text, author, netServer.chatFormatter.format(author, text));
        Translator.translate(author, text);

        sendMessage(botChannel, author.plainName(), text);
        return null;
    }
}