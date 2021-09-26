package pandorum.events;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import mindustry.game.EventType;
import pandorum.comp.DiscordWebhookManager;

public class ServerLoadListener {
    public static void call(final EventType.ServerLoadEvent event) {
        WebhookEmbedBuilder banEmbedBuilder = new WebhookEmbedBuilder()
                .setColor(0x05DDF5)
                .setTitle(new WebhookEmbed.EmbedTitle("Сервер был запущен!", null));
        DiscordWebhookManager.client.send(banEmbedBuilder.build());
    }
}