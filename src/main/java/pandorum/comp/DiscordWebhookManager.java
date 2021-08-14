package pandorum.comp;

import club.minnced.discord.webhook.WebhookClient;
import pandorum.PandorumPlugin;

public class DiscordWebhookManager {
    public static WebhookClient client = WebhookClient.withUrl(PandorumPlugin.config.DiscordWebhookLink);
}