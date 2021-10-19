package pandorum.events;

import arc.struct.Seq;
import arc.util.Strings;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.world.Tile;
import pandorum.PandorumPlugin;
import pandorum.comp.Config.PluginType;
import pandorum.comp.DiscordWebhookManager;
import org.bson.Document;
import pandorum.entry.DepositEntry;
import pandorum.entry.HistoryEntry;

import static pandorum.Misc.bundled;

public class DepositListener {
    public static void call(final EventType.DepositEvent event) {
        if (PandorumPlugin.config.type == PluginType.other) return;
        if (event.tile.block() == Blocks.thoriumReactor && event.item == Items.thorium && event.player.team().cores().contains(c -> event.tile.dst(c.x, c.y) < PandorumPlugin.config.alertDistance)) {
            Groups.player.each(p -> {
                Document playerInfo = PandorumPlugin.createInfo(p);
                if (playerInfo.getBoolean("alerts")) bundled(p, "events.withdraw-thorium", event.player.coloredName(), event.tile.tileX(), event.tile.tileY());
            });

            WebhookEmbedBuilder depositEmbedBuilder = new WebhookEmbedBuilder()
                    .setColor(0xE81CFF)
                    .setTitle(new WebhookEmbed.EmbedTitle("ВНИМАНИЕ!!! Данный игрок положил торий в реактор возле ядра!", null))
                    .addField(new WebhookEmbed.EmbedField(true, "Позиция", String.format("X: %s, Y: %s", event.tile.tileX(), event.tile.tileY())))
                    .addField(new WebhookEmbed.EmbedField(true, "Никнейм", Strings.stripColors(event.player.name)));
            DiscordWebhookManager.client.send(depositEmbedBuilder.build());
        }

        HistoryEntry entry = new DepositEntry(event.player.coloredName(), event.tile.block, event.item, event.amount);
        Seq<Tile> linkedTiles = event.tile.tile.getLinkedTiles(new Seq<>());
        for (Tile tile : linkedTiles) {
            PandorumPlugin.history[tile.x][tile.y].add(entry);
        }
    }
}
