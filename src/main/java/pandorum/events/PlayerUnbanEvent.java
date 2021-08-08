package pandorum.events;

import mindustry.game.EventType;
import mindustry.net.Administration.*;
import mindustry.Vars;
import pandorum.comp.*;
import pandorum.PandorumPlugin;

import java.io.IOException;
import java.awt.Color;

public class PlayerUnbanEvent {
    public static void call(final EventType.PlayerUnbanEvent event) {
        if (PandorumPlugin.config.hasWebhookLink()) {
            PlayerInfo info = Vars.netServer.admins.getInfo(event.uuid);
            if (info == null) return;
            new Thread(() -> {
                Webhook wh = new Webhook(PandorumPlugin.config.DiscordWebhookLink);
                wh.setUsername("Сервер");
                wh.addEmbed(new Webhook.EmbedObject()
                        .setTitle("Игрок получил разбан!")
                        .addField("Никнейм:", info.lastName, false)
                        .setColor(new Color(0, 0, 255)));
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
