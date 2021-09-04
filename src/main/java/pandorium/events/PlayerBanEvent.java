package pandorium.events;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.net.Administration.PlayerInfo;
import pandorium.comp.DiscordWebhookManager;

public class PlayerBanEvent {
    public static void call(final EventType.PlayerBanEvent event) {
        PlayerInfo info = Vars.netServer.admins.getInfo(event.uuid);
        if (info == null) return;
        WebhookEmbedBuilder banEmbedBuilder = new WebhookEmbedBuilder()
                .setColor(0xFF0000)
                .setTitle(new WebhookEmbed.EmbedTitle("Игрок был заблокирован!", null))
                .addField(new WebhookEmbed.EmbedField(true, "Никнейм", info.lastName))
                .addField(new WebhookEmbed.EmbedField(true, "UUID", event.uuid))
                .addField(new WebhookEmbed.EmbedField(true, "IP", info.lastIP));
        DiscordWebhookManager.client.send(banEmbedBuilder.build());
    }
}