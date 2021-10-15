package pandorum.commands.server;

import arc.util.Log;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import mindustry.gen.Groups;
import mindustry.gen.Unitc;
import pandorum.comp.DiscordWebhookManager;

public class DespawnCommand {
    public static void run(final String[] args) {
        int amount = Groups.unit.size();
        Groups.unit.each(Unitc::kill);
        Log.info("Ты убил @ юнитов!", amount);
        WebhookEmbedBuilder despwEmbedBuilder = new WebhookEmbedBuilder()
                .setColor(0xFF0000)
                .setTitle(new WebhookEmbed.EmbedTitle("Все юниты убиты!", null));
        DiscordWebhookManager.client.send(despwEmbedBuilder.build());
    }
}
