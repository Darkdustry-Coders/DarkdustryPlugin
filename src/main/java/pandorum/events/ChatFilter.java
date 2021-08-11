package pandorum.events;

import arc.util.*;
import mindustry.gen.Player;
import mindustry.gen.Call;

import pandorum.PandorumPlugin;
import pandorum.comp.*;
import pandorum.Misc;

public class ChatFilter {
    public static String call(final Player player, final String text) {
        Call.sendMessage(Misc.colorizedName(player) + " [lightgray]>>[white] " + text);
        Log.info(player.name + "&ly > " + text);
        DiscordSender.send(player.name, text);
        return null;
    }
}
