package pandorum.comp;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import pandorum.PandorumPlugin;

public class DiscordWebhookManager {
    public static WebhookClient client = new WebhookClientBuilder(PandorumPlugin.config.DiscordWebhookLink.replace("discord", "discordapp"))
        .setWait(false)
        .build();
}