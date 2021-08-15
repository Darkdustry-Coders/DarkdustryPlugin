package pandorum.events;

import arc.util.Log;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import mindustry.game.EventType;
import pandorum.PandorumPlugin;
import pandorum.comp.DiscordWebhookManager;

public class ServerLoadEvent {
    public static void call(final EventType.ServerLoadEvent event) {
        Log.info(PandorumPlugin.config.DiscordWebhookLink);
        WebhookEmbedBuilder banEmbedBuilder = new WebhookEmbedBuilder()
                .setColor(0x05DDF5)
                .setTitle(new WebhookEmbed.EmbedTitle("Сервер был запущен!", null));
        DiscordWebhookManager.client.send(banEmbedBuilder.build());
    }
}