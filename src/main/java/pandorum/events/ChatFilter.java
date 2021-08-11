package pandorum.events;

import arc.util.*;
import mindustry.gen.Player;
import mindustry.gen.Call;

import pandorum.PandorumPlugin;
import pandorum.comp.*;
import pandorum.Misc;

public class ChatFilter {
    public static String call(final Player player, final String text) {
        DiscordSender.send(Strings.stripColors(player.name), text.replaceAll("@", ""));
        return text;
    }
}
