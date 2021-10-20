package pandorum.events;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import org.bson.Document;
import pandorum.PandorumPlugin;
import pandorum.comp.Config.PluginType;
import pandorum.comp.DiscordWebhookManager;

public class GameOverListener {
    public static void call(final EventType.GameOverEvent event) {
        WebhookEmbedBuilder gameoverEmbedBuilder = new WebhookEmbedBuilder()
                .setColor(0x05DDF5)
                .setTitle(new WebhookEmbed.EmbedTitle("Игра окончена! Победила команда " + event.winner.name + "! Загружаю новую карту...", null));
        DiscordWebhookManager.client.send(gameoverEmbedBuilder.build());

        Groups.player.each(p -> {
            Document playerInfo = PandorumPlugin.createInfo(p);
            int gamesPlayed = playerInfo.getInteger("gamesPlayed") + 1;
            playerInfo.replace("gamesPlayed", gamesPlayed);
            PandorumPlugin.savePlayerStats(p.uuid());
        });

        if (PandorumPlugin.config.type == PluginType.pvp) PandorumPlugin.surrendered.clear();
        PandorumPlugin.votesRTV.clear();
        PandorumPlugin.votesVNW.clear();
    }
}
