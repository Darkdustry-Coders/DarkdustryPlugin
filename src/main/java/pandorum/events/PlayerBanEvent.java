package pandorum.events;

import mindustry.game.EventType;
import mindustry.Vars;
import pandorum.comp.*;
import pandorum.PandorumPlugin;

import java.io.IOException;
import java.awt.Color;

public class PlayerBanEvent {
    public static void call(final EventType.PlayerBanEvent event) {
        if (PandorumPlugin.config.hasWebhookLink()) {
            PlayerInfo info = Vars.netServer.admins.getInfo(event.uuid);
            if (info == null) return;
            new Thread(() -> {
                Webhook wh = new Webhook(PandorumPlugin.config.DiscordWebhookLink);
                wh.setUsername("Сервер");
                wh.addEmbed(new Webhook.EmbedObject()
                        .setTitle("Игрок получил бан!")
                        .addField("Никнейм:", info.lastName, false)
                        .setColor(new Color(255, 0, 0)));
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
