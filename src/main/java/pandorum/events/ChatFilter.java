package pandorum.events;

import arc.util.Strings;
import mindustry.gen.Player;
import pandorum.comp.DiscordWebhookManager;

public class ChatFilter {
    public static String call(final Player player, final String text) {
        DiscordWebhookManager.client.send(String.format("**[%s]:** %s", Strings.stripColors(player.name), text.replaceAll("@", "")));
        return text;
    }
}
