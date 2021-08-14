package pandorum.events;

import arc.util.Strings;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import pandorum.PandorumPlugin;
import pandorum.comp.Config.PluginType;
import pandorum.comp.DiscordWebhookManager;

import static pandorum.Misc.bundled;
import static pandorum.Misc.colorizedName;

public class DepositEvent {
    public static void call(final EventType.DepositEvent event) {
        if(PandorumPlugin.config.type == PluginType.other) return;
        if(event.tile.block() == Blocks.thoriumReactor && event.item == Items.thorium && event.player.team().cores().contains(c -> event.tile.dst(c.x, c.y) < PandorumPlugin.config.alertDistance)){
            Groups.player.each(p -> !PandorumPlugin.alertIgnores.contains(p.uuid()), p -> bundled(p, "events.withdraw-thorium", colorizedName(event.player), event.tile.tileX(), event.tile.tileY()));
            WebhookEmbedBuilder banEmbedBuilder = new WebhookEmbedBuilder()
                    .setColor(0xE81CFF)
                    .setTitle(new WebhookEmbed.EmbedTitle("ВНИМАНИЕ!!! Данный игрок положил торий в реактор возле ядра!", null))
                    .addField(new WebhookEmbed.EmbedField(true, "Позиция", String.format("X: %dY: %d", event.tile.tileX(), event.tile.tileY())))
                    .addField(new WebhookEmbed.EmbedField(true, "Никнейм", Strings.stripColors(event.player.name)));
            DiscordWebhookManager.client.send(banEmbedBuilder.build());
        }
    }
}