package pandorum.events;

import arc.util.Strings;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import org.bson.Document;
import pandorum.PandorumPlugin;
import pandorum.comp.Config.PluginType;
import pandorum.comp.DiscordWebhookManager;

import static pandorum.Misc.bundled;

public class BuildSelectListener {
    public static void call(final EventType.BuildSelectEvent event) {
        if (PandorumPlugin.config.type == PluginType.other) return;

        if (!event.breaking && event.builder != null && event.builder.buildPlan() != null && event.builder.buildPlan().block == Blocks.thoriumReactor && event.builder.isPlayer() && event.team.cores().contains(c -> event.tile.dst(c.x, c.y) < PandorumPlugin.config.alertDistance)) {
            Player builder = event.builder.getPlayer();

            if (PandorumPlugin.interval.get(750f)) {
                Groups.player.each(p -> {
                    Document playerInfo = PandorumPlugin.createInfo(p);
                    if (playerInfo.getBoolean("alerts")) bundled(p, "events.alert", builder.coloredName(), event.tile.x, event.tile.y);
                });

                WebhookEmbedBuilder alertEmbedBuilder = new WebhookEmbedBuilder()
                        .setColor(0xE81CFF)
                        .setTitle(new WebhookEmbed.EmbedTitle("ВНИМАНИЕ!!! Данный игрок начал строить ториевый реактор возле ядра!", null))
                        .addField(new WebhookEmbed.EmbedField(true, "Позиция", String.format("X: %d, Y: %d", event.tile.x, event.tile.y)))
                        .addField(new WebhookEmbed.EmbedField(true, "Никнейм", Strings.stripColors(builder.name())));
                DiscordWebhookManager.client.send(alertEmbedBuilder.build());
            }
        }
    }
}
