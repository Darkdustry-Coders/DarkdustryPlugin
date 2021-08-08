package pandorum.events;

import mindustry.game.EventType;
import mindustry.Vars;
import mindustry.world.Tile;

import pandorum.PandorumPlugin;
import pandorum.struct.*;
import pandorum.comp.Config.PluginType;
import pandorum.comp.*;

import java.time.Duration;
import java.io.IOException;
import java.awt.Color;

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

        if (PandorumPlugin.config.hasWebhookLink()) {
            new Thread(() -> {
                Webhook wh = new Webhook(PandorumPlugin.config.DiscordWebhookLink);
                wh.setUsername("Сервер");
                wh.addEmbed(new Webhook.EmbedObject()
                        .setTitle("Загружена новая карта!")
                        .addField("Название:", Vars.state.map.name(), false)
                        .setColor(new Color(0, 222, 222)));
                try {
                    wh.execute();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } finally {
                    Thread.currentThread().interrupt();
                }
                return;
            }).start();
        }
    }
}
