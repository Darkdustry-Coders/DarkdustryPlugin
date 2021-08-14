package pandorum.events;

import arc.struct.*;
import mindustry.game.EventType;
import mindustry.world.Tile;
import mindustry.Vars;
import mindustry.world.blocks.logic.LogicBlock;

import pandorum.PandorumPlugin;
import pandorum.entry.*;
import pandorum.struct.*;

public class ConfigEvent {
    public static void call(final EventType.ConfigEvent event) {
        if(event.tile.block instanceof LogicBlock || event.player == null || event.tile.tileX() > Vars.world.width() || event.tile.tileX() > Vars.world.height()) return;

        CacheSeq<HistoryEntry> entries = PandorumPlugin.history[event.tile.tileX()][event.tile.tileY()];
        boolean connect = true;

        HistoryEntry last = entries.peek();
        if(!entries.isEmpty() && last instanceof ConfigEntry){
            ConfigEntry lastConfigEntry = (ConfigEntry)last;

            connect = !event.tile.getPowerConnections(new Seq<>()).isEmpty() &&
                    !(lastConfigEntry.value instanceof Integer && event.value instanceof Integer &&
                    (int)lastConfigEntry.value == (int)event.value && lastConfigEntry.connect);
        } 

        HistoryEntry entry = new ConfigEntry(event, connect);

        Seq<Tile> linkedTile = event.tile.tile.getLinkedTiles(new Seq<>());
        for(Tile tile : linkedTile){
            PandorumPlugin.history[tile.x][tile.y].add(entry);
        }
    }
}