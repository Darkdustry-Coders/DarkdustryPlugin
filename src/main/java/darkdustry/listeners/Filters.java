package darkdustry.listeners;

import arc.util.Log;
import darkdustry.components.Translator;
import darkdustry.discord.Bot;
import darkdustry.features.history.*;
import mindustry.gen.Player;
import mindustry.net.Administration.PlayerAction;

import static darkdustry.PluginVars.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;
import static mindustry.net.Administration.ActionType.*;

public class Filters {

    public static String chat(Player author, String text) {
        int sign = voteChoice(text);
        if (sign == 0 || vote == null) {
            Log.info("&fi@: @", "&lc" + author.plainName(), "&lw" + text);

            author.sendMessage(netServer.chatFormatter.format(author, text), author, text);
            Translator.translate(author, text);

            Bot.sendMessage(author.plainName(), text);
        } else if (!alreadyVoted(author, vote))
            vote.vote(author, sign);

        return null;
    }
}