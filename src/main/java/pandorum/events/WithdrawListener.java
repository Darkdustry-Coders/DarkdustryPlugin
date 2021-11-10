package pandorum.events;

import arc.struct.Seq;
import mindustry.game.EventType;
import mindustry.world.Tile;
import pandorum.PandorumPlugin;
import pandorum.comp.Config;
import pandorum.entry.HistoryEntry;
import pandorum.entry.WithdrawEntry;

public class WithdrawListener {
    public static void call(final EventType.WithdrawEvent event) {
        if (PandorumPlugin.config.mode != Config.Gamemode.hexed && PandorumPlugin.config.mode != Config.Gamemode.hub && PandorumPlugin.config.mode != Config.Gamemode.castle) {
            HistoryEntry entry = new WithdrawEntry(event);
            Seq<Tile> linkedTiles = event.tile.tile.getLinkedTiles(new Seq<>());
            for (Tile tile : linkedTiles) {
                PandorumPlugin.history[tile.x][tile.y].add(entry);
            }
        }
    }
}
