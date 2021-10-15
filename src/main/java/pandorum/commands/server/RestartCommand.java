package pandorum.commands.server;

import arc.util.Log;
import arc.util.Timer;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import mindustry.gen.Groups;
import pandorum.Misc;
import pandorum.comp.DiscordWebhookManager;

public class RestartCommand {
    public static void run(final String[] args) {
        Log.info("Перезапуск сервера...");
        WebhookEmbedBuilder restartEmbedBuilder = new WebhookEmbedBuilder()
                .setColor(0xFF0000)
                .setTitle(new WebhookEmbed.EmbedTitle("Сервер выключился для перезапуска!", null));
        DiscordWebhookManager.client.send(restartEmbedBuilder.build());

        Groups.player.each(Misc::connectToHub);
        Timer.schedule(() -> System.exit(2), 5f);
    }
}
