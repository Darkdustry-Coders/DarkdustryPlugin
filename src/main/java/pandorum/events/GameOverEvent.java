package pandorum.events;

import mindustry.game.EventType;
import pandorum.PandorumPlugin;
import pandorum.comp.Config.PluginType;
import pandorum.comp.*;

import java.io.IOException;
import java.awt.Color;

public class GameOverEvent {
    public static void call(final EventType.GameOverEvent event) {
        if (PandorumPlugin.config.hasWebhookLink()) {
            new Thread(() -> {
                Webhook wh = new Webhook(PandorumPlugin.config.DiscordWebhookLink);
                wh.setUsername("Сервер");
                wh.addEmbed(new Webhook.EmbedObject()
                        .setTitle("Игра окончена!")         
                        .setColor(new Color(0, 222, 222)));
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

        if(PandorumPlugin.config.type == PluginType.other) return;
        else if(PandorumPlugin.config.type == PluginType.pvp) PandorumPlugin.surrendered.clear();
        PandorumPlugin.votesRTV.clear();
        PandorumPlugin.votesVNW.clear();
    }
}
