package pandorium.events;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import mindustry.game.EventType;
import mindustry.game.Team;
import pandorium.PandorumPlugin;
import pandorium.comp.Config.PluginType;
import pandorium.comp.DiscordWebhookManager;

public class GameOverEvent {
    public static void call(final EventType.GameOverEvent event) {
        WebhookEmbedBuilder banEmbedBuilder = new WebhookEmbedBuilder()
                .setColor(0x05DDF5)
                .setTitle(new WebhookEmbed.EmbedTitle("Игра окончена!" + (event.winner == Team.derelict ? "" : " Победила команда " + event.winner.name + "!"), null));
        DiscordWebhookManager.client.send(banEmbedBuilder.build());

        if(PandorumPlugin.config.type == PluginType.other) return;
        else if(PandorumPlugin.config.type == PluginType.pvp) PandorumPlugin.surrendered.clear();
        PandorumPlugin.votesRTV.clear();
        PandorumPlugin.votesVNW.clear();
    }
}
