package pandorum.events;

import arc.util.Log;
import mindustry.gen.Player;
import mindustry.gen.Call;

import pandorum.Misc.*;
import pandorum.PandorumPlugin;

public class ChatFilter {
    public static String call(final Player player, final String text) {
        Call.sendMessage(player.name + " [lightgray]>>[white] " + text);
        Log.info(player.name + "&ly > " + text);
        return null;
    }
}
