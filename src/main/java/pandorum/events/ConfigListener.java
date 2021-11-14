package pandorum.events;

import arc.struct.Seq;
import arc.util.Log;
import arc.util.Pack;
import mindustry.game.EventType;
import mindustry.world.Tile;
import mindustry.Vars;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.blocks.power.PowerNode;

import pandorum.PandorumPlugin;
import pandorum.comp.Config;
import pandorum.entry.HistoryEntry;
import pandorum.entry.ConfigEntry;
import pandorum.struct.CacheSeq;

public class ConfigListener {
    public static void call(final EventType.ConfigEvent event) {
        if (event.tile.block instanceof LogicBlock || event.player == null || event.tile.tileX() > Vars.world.width() || event.tile.tileX() > Vars.world.height() || PandorumPlugin.config.mode == Config.Gamemode.hexed || PandorumPlugin.config.mode == Config.Gamemode.hub || PandorumPlugin.config.mode == Config.Gamemode.castle) return;

        CacheSeq<HistoryEntry> entries = PandorumPlugin.history[event.tile.tileX()][event.tile.tileY()];
        int connections = event.tile.power.links.size;
        boolean connect = connections > 0;

        if (!entries.isEmpty() && entries.peek() instanceof ConfigEntry lastConfigEntry && lastConfigEntry.value instanceof Long l) {
            Log.debug("old conns: @, now conns: @", Pack.leftInt(l), connections);
            // У множественного подключения/отключения немного другая логика и она требует более сложный метод поиска изменения
            boolean isBatch = entries.size >= 2 && entries.get(entries.size - 2) instanceof ConfigEntry e
                    && e.value instanceof Long l1 && Pack.leftInt(l1) != connections
                    && e.block instanceof PowerNode;

            Log.debug("isBatch = " + isBatch);

            connect = event.tile.block instanceof PowerNode node ?
                    isBatch ? isLastUniqueCount(entries, l, node.maxNodes) : connections > Pack.leftInt(l) :
                    event.value instanceof Integer i && i >= 0;
        }

        HistoryEntry entry = new ConfigEntry(event, connect);

        Seq<Tile> linkedTiles = event.tile.tile.getLinkedTiles(new Seq<>());
        for (Tile tile : linkedTiles) {
            PandorumPlugin.history[tile.x][tile.y].add(entry);
        }
    }

    private static boolean isLastUniqueCount(CacheSeq<HistoryEntry> entries, long lastCount, int maxSearchBound) {
        for (int i = entries.size - 2; i >= maxSearchBound; i--) {
            if (entries.get(i) instanceof ConfigEntry pre && pre.value instanceof Long preCfg) {
                // Log.debug("pre: @, post: @", Pack.leftInt(preCfg), Pack.leftInt(lastCount));
                if (Pack.leftInt(lastCount) > Pack.leftInt(preCfg)) {
                    return true;
                }
            }
        }
        return false;
    }
}
