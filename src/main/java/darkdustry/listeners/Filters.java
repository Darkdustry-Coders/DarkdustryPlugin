package darkdustry.listeners;

import arc.util.*;
import darkdustry.components.Translator;
import darkdustry.features.history.*;
import mindustry.gen.Player;
import mindustry.net.Administration.*;

//import static arc.util.Strings.stripColors;
import static darkdustry.PluginVars.vote;
import static darkdustry.discord.Bot.*;
import static darkdustry.utils.Checks.alreadyVoted;
import static darkdustry.utils.Utils.voteChoice;
import static mindustry.Vars.netServer;

public class Filters {

    public static boolean action(PlayerAction action) {
        if (History.enabled() && action.type == ActionType.rotate) History.put(new RotateEntry(action), action.tile);
        return true;
    }

    public static String chat(Player author, String text) {
        int sign = voteChoice(text);
        if (sign != 0 && vote != null && !alreadyVoted(author, vote)) {
            vote.vote(author, sign);
            return null;
        }

        Log.info("&fi@: @", "&lc" + author.plainName(), "&lw" + text);

        author.sendMessage(netServer.chatFormatter.format(author, text), author, text);
        Translator.translate(author, text);

        sendMessage(botChannel, "**@:** @", author.plainName(), text);
        return null;
    }
}