package pandorum.events;

import arc.struct.Seq;
import arc.util.Pack;
import mindustry.game.EventType;
import mindustry.gen.Building;
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
        boolean connect = true;

        HistoryEntry last = entries.peek();
        if (!entries.isEmpty() && last instanceof ConfigEntry lastConfigEntry) {

            Seq<Building> conns = event.tile.getPowerConnections(new Seq<>());
            connect = lastConfigEntry.value instanceof Long &&
                    (conns.any() && event.tile.block instanceof PowerNode &&
                    conns.size > Pack.leftInt((Long) lastConfigEntry.value) ||
                    event.value instanceof Integer && (int) event.value >= 0);
        }

        HistoryEntry entry = new ConfigEntry(event, connect);

        Seq<Tile> linkedTiles = event.tile.tile.getLinkedTiles(new Seq<>());
        for (Tile tile : linkedTiles) {
            PandorumPlugin.history[tile.x][tile.y].add(entry);
        }
    }
}
