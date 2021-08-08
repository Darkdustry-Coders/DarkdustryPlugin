package pandorum.events;

import mindustry.game.EventType;
import pandorum.comp.*;

import java.io.IOException;
import java.awt.Color;

public class ServerLoadEvent {
    public static void call(final EventType.ServerLoadEvent event) {
        if (PandorumPlugin.config.hasWebhookLink()) {
            new Thread(() -> {
                Webhook wh = new Webhook(PandorumPlugin.config.DiscordWebhookLink);
                wh.setUsername("Сервер");
                wh.addEmbed(new Webhook.EmbedObject()
                        .setTitle("Сервер запущен!")         
                        .setColor(new Color(0, 255, 0)));
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
