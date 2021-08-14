package pandorum.events;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.world.Tile;
import pandorum.PandorumPlugin;
import pandorum.comp.Config.PluginType;
import pandorum.comp.DiscordWebhookManager;
import pandorum.struct.CacheSeq;
import pandorum.struct.Seqs;

import java.time.Duration;

public class WorldLoadEvent {
    public static void call(final EventType.WorldLoadEvent event) {
        if(PandorumPlugin.config.type == PluginType.sand) PandorumPlugin.timer.clear();
        PandorumPlugin.history = new CacheSeq[Vars.world.width()][Vars.world.height()];

        for(Tile tile : Vars.world.tiles){
            PandorumPlugin.history[tile.x][tile.y] = Seqs.newBuilder()
                    .maximumSize(PandorumPlugin.config.historyLimit)
                    .expireAfterWrite(Duration.ofMillis(PandorumPlugin.config.expireDelay))
                    .build();
        }
        WebhookEmbedBuilder banEmbedBuilder = new WebhookEmbedBuilder()
                .setColor(0x05DDF5)
                .setTitle(new WebhookEmbed.EmbedTitle(String.format("Загружена новая карта%s!", Vars.state.map), null));
        DiscordWebhookManager.client.send(banEmbedBuilder.build());
    }
}