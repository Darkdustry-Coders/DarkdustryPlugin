package pandorum.comp;

import java.awt.*;
import java.io.*;

import pandorum.PandorumPlugin;

public class DiscordSender {
    public static void sendToDiscord(String name, String title, Color color) {
        if (PandorumPlugin.config.hasWebhookLink()) {
            new Thread(() -> {
                Webhook wh = new Webhook(PandorumPlugin.config.DiscordWebhookLink);
                wh.setUsername(name);
                wh.addEmbed(new Webhook.EmbedObject()
                         .setTitle(title)
                         .setColor(color));
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
    }
}
