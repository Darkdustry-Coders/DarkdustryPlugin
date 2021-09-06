package pandorum.events;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.net.Administration;
import pandorum.comp.DiscordWebhookManager;
import pandorum.models.PlayerInfo;

import org.bson.Document;

public class PlayerUnbanEvent {
    public static void call(final EventType.PlayerUnbanEvent event) {
        Administration.PlayerInfo info = Vars.netServer.admins.getInfo(event.uuid);
        if (info != null) {
            WebhookEmbedBuilder banEmbedBuilder = new WebhookEmbedBuilder()
                    .setColor(0xFF0000)
                    .setTitle(new WebhookEmbed.EmbedTitle("Игрок был разбанен!", null))
                    .addField(new WebhookEmbed.EmbedField(true, "Никнейм", info.lastName))
                    .addField(new WebhookEmbed.EmbedField(true, "UUID", event.uuid))
                    .addField(new WebhookEmbed.EmbedField(true, "IP", info.lastIP));
            DiscordWebhookManager.client.send(banEmbedBuilder.build());
        }
        Document playerInfo = PandorumPlugin.playersInfo.find((playerInfo2) -> playerInfo2.getString("uuid").equals(event.uuid));
        if (playerInfo == null) {
            playerInfo = PandorumPlugin.playerInfoSchema.create(event.uuid, "IDK", false);
            PandorumPlugin.playersInfo.add(playerInfo);
        }
        playerInfo.replace("banned", true);
        PandorumPlugin.savePlayerStats(event.uuid);
    }
}
