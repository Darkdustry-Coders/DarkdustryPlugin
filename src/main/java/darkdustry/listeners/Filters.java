package darkdustry.listeners;

import arc.util.Log;
import darkdustry.components.Translator;
import darkdustry.features.history.*;
import mindustry.gen.Player;
import mindustry.net.Administration.PlayerAction;

import static darkdustry.PluginVars.vote;
import static darkdustry.discord.Bot.*;
import static darkdustry.utils.Checks.alreadyVoted;
import static darkdustry.utils.Utils.voteChoice;
import static mindustry.Vars.netServer;
import static mindustry.net.Administration.ActionType.rotate;

public class Filters {

    public static boolean action(PlayerAction action) {
        if (History.enabled() && action.type == rotate)
            History.put(new RotateEntry(action), action.tile);
        return true;
    }

    public static String chat(Player author, String text) {
        int sign = voteChoice(text);
        if (sign != 0 && vote != null && !alreadyVoted(author, vote)) {
            vote.vote(author, sign);
            return null;
        }

        Log.info("&y@: &lb@", author.plainName(), text);

        author.sendMessage(netServer.chatFormatter.format(author, text), author, text);
        Translator.translate(author, text);

        sendMessage(botChannel, author.plainName(), text);
        return null;
    }
}