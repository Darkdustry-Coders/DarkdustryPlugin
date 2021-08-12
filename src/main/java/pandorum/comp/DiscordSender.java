package pandorum.comp;

import java.awt.*;

import arc.struct.Queue;
import arc.util.Timer;
import pandorum.PandorumPlugin;
import webhook.Webhook;
import webhook.embed.Embed;

public class DiscordSender {

    private static final Queue<Webhook> webhooks = new Queue<>();

    static {
        Timer.schedule(() -> {
            if (!webhooks.isEmpty()) {
                webhooks.removeFirst().execute();
            }
        }, 0, 1);
    }

    // Эмбед без полей. Только название.
    public static void send(String name, String title, Color color) {
        if (PandorumPlugin.config.hasWebhookLink()) {
            webhooks.add(new Webhook(PandorumPlugin.config.DiscordWebhookLink)
                    .setUsername(name)
                    .addEmbed(new Embed()
                            .setTitle(title)
                            .setColor(color)));
        }
    }

    // Эмбед с одним полем.
    public static void send(String name, String title, String fieldName, String fieldContent, Color color) {
        if (PandorumPlugin.config.hasWebhookLink()) {
            webhooks.add(new Webhook(PandorumPlugin.config.DiscordWebhookLink)
                    .setUsername(name)
                    .addEmbed(new Embed()
                            .setTitle(title)
                            .addField(fieldName, fieldContent)
                            .setColor(color)));
        }
    }

    // Эмбед с двумя полями.
    public static void send(String name, String title, String fieldName1, String fieldContent1, String fieldName2, String fieldContent2, Color color) {
        if (PandorumPlugin.config.hasWebhookLink()) {
            webhooks.add(new Webhook(PandorumPlugin.config.DiscordWebhookLink)
                    .setUsername(name)
                    .addEmbed(new Embed()
                            .setTitle(title)
                            .addField(fieldName1, fieldContent1)
                            .addField(fieldName2, fieldContent2)
                            .setColor(color)));
        }
    }

    public static void send(String name, String text) {
        if (PandorumPlugin.config.hasWebhookLink()) {
            webhooks.add(new Webhook(PandorumPlugin.config.DiscordWebhookLink)
                    .setUsername(name)
                    .setContent(text));
        }
    }
}
