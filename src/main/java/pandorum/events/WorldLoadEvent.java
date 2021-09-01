package pandorum.events;

import java.time.Duration;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import mindustry.Vars;
import mindustry.gen.Groups;
import mindustry.game.EventType;
import mindustry.world.Tile;
import pandorum.PandorumPlugin;
import pandorum.comp.Config.PluginType;
import pandorum.comp.DiscordWebhookManager;
import pandorum.struct.CacheSeq;
import pandorum.struct.Seqs;

public class WorldLoadEvent {
    @SuppressWarnings("unchecked")
    public static void call(final EventType.WorldLoadEvent event) {
        if(PandorumPlugin.config.type == PluginType.sand) PandorumPlugin.timer.clear();
        if (Groups.player.size() == 0) Vars.state.serverPaused = true;
        PandorumPlugin.history = new CacheSeq[Vars.world.width()][Vars.world.height()];

        for(Tile tile : Vars.world.tiles){
            PandorumPlugin.history[tile.x][tile.y] = Seqs.newBuilder()
                    .maximumSize(PandorumPlugin.config.historyLimit)
                    .expireAfterWrite(Duration.ofMillis(PandorumPlugin.config.expireDelay))
                    .build();
        }
        WebhookEmbedBuilder banEmbedBuilder = new WebhookEmbedBuilder()
                .setColor(0x05DDF5)
                .setTitle(new WebhookEmbed.EmbedTitle("Загружена новая карта!", null));
        DiscordWebhookManager.client.send(banEmbedBuilder.build());
    }
}
