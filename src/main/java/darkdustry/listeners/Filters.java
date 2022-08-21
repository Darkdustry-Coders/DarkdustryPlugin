package darkdustry.listeners;

import arc.util.Log;
import mindustry.gen.Player;
import mindustry.net.Administration.*;
import darkdustry.discord.Bot;
import darkdustry.features.Translator;
import darkdustry.features.history.History;
import darkdustry.features.history.RotateEntry;

import static arc.util.Strings.*;
import static darkdustry.PluginVars.vote;
import static darkdustry.utils.Checks.alreadyVoted;
import static darkdustry.utils.Utils.voteChoice;
import static mindustry.Vars.*;

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

        Log.info("&fi@: @", "&lc" + author.name, "&lw" + text);

        author.sendMessage(netServer.chatFormatter.format(author, text), author, text);
        Translator.translate(author, text);

        Bot.sendMessage(Bot.botChannel, "@ » @", stripColors(author.name), stripColors(text));
        return null;
    }
}
