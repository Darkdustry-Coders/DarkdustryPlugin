package darkdustry.listeners;

import arc.util.Log;
import darkdustry.components.Translator;
import darkdustry.discord.Bot;
import mindustry.gen.Player;

import static darkdustry.PluginVars.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;

public class Filters {

    public static String chat(Player from, String text) {
        int sign = voteChoice(text);
        if (sign == 0 || vote == null) {
            Log.info("&fi@: @", "&lc" + from.plainName(), "&lw" + text);
            Translator.translate(from, text);

            Bot.sendMessage(from.plainName(), text);
            return null;
        }

        if (!alreadyVoted(from, vote)) vote.vote(from, sign);
        return null;
    }
}