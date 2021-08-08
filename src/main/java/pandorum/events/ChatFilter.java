package pandorum.events;

import arc.util.Log;
import mindustry.gen.Player;
import mindustry.gen.Call;

import pandorum.PandorumPlugin;
import pandorum.comp.*;

import java.io.IOException;

public class ChatFilter {
    public static String call(final Player player, final String text) {
        Call.sendMessage(player.name + " [lightgray]>>[white] " + text);
        Log.info(player.name + "&ly > " + text);

        if (PandorumPlugin.config.hasWebhookLink()) {
            new Thread(() -> {
                Webhook wh = new Webhook(PandorumPlugin.config.DiscordWebhookLink);
                wh.setUsername(player.name);
                wh.setContent(text);
                try {
                    wh.execute();
                } catch (IOException ioException) {
                     ioException.printStackTrace();
                } finally {
                    Thread.currentThread().interrupt();
                }
                return;
             }).start();
        }
        return null;
    }
}
