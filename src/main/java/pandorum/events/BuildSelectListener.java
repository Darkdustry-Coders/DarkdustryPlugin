package pandorum.events;

import arc.util.Strings;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.PandorumPlugin;
import pandorum.comp.Config.PluginType;
import pandorum.comp.DiscordWebhookManager;

import static pandorum.Misc.bundled;
import static pandorum.Misc.colorizedName;

public class BuildSelectListener {
    public static void call(final EventType.BuildSelectEvent event) {
        if (PandorumPlugin.config.type == PluginType.other) return;
        if (!event.breaking && event.builder != null && event.builder.buildPlan() != null &&
            event.builder.buildPlan().block == Blocks.thoriumReactor && event.builder.isPlayer() &&
            event.team.cores().contains(c -> event.tile.dst(c.x, c.y) < PandorumPlugin.config.alertDistance)) {
            Player target = event.builder.getPlayer();

            if (PandorumPlugin.interval.get(300)) {
                Groups.player.each(p -> !PandorumPlugin.alertIgnores.contains(p.uuid()), p -> bundled(p, "events.alert", colorizedName(target), event.tile.x, event.tile.y));
                WebhookEmbedBuilder alertEmbedBuilder = new WebhookEmbedBuilder()
                        .setColor(0xE81CFF)
                        .setTitle(new WebhookEmbed.EmbedTitle("ВНИМАНИЕ!!! Данный игрок начал строить ториевый реактор возле ядра!", null))
                        .addField(new WebhookEmbed.EmbedField(true, "Позиция", String.format("X: %d, Y: %d", event.tile.x, event.tile.y)))
                        .addField(new WebhookEmbed.EmbedField(true, "Никнейм", Strings.stripColors(target.name())));
                DiscordWebhookManager.client.send(alertEmbedBuilder.build());
            }
        }
    }
}
