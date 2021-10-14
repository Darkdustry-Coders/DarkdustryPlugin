package pandorum.events;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import mindustry.game.EventType;
import mindustry.net.Administration.PlayerInfo;
import pandorum.comp.DiscordWebhookManager;

import static mindustry.Vars.netServer;

public class PlayerUnbanListener {
    public static void call(final EventType.PlayerUnbanEvent event) {
        PlayerInfo info = netServer.admins.getInfo(event.uuid);
        if (info != null) {
            WebhookEmbedBuilder unbanEmbedBuilder = new WebhookEmbedBuilder()
                    .setColor(0xFF0000)
                    .setTitle(new WebhookEmbed.EmbedTitle("Игрок был разбанен!", null))
                    .addField(new WebhookEmbed.EmbedField(true, "Никнейм", info.lastName))
                    .addField(new WebhookEmbed.EmbedField(true, "UUID", event.uuid))
                    .addField(new WebhookEmbed.EmbedField(true, "IP", info.lastIP));
            DiscordWebhookManager.client.send(unbanEmbedBuilder.build());
        }
    }
}
