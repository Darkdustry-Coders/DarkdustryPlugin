package pandorum.events;

import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.world.Tile;

import pandorum.PandorumPlugin;
import pandorum.comp.*;
import pandorum.struct.*;
import pandorum.comp.Config.PluginType;
import static pandorum.Misc.*;

public class BuildSelectEvent {
    public static void call(final EventType.BuildSelectEvent event) {
        if(PandorumPlugin.activeHistoryPlayers.contains(event.player.uuid())){
            CacheSeq<HistoryEntry> entries = PandorumPlugin.history[event.tile.x][event.tile.y];
            StringBuilder message = new StringBuilder(Bundle.format("events.history.title", findLocale(event.player.locale), event.tile.x, event.tile.y));

            entries.cleanUp();
            if(entries.isOverflown()) message.append(Bundle.get("events.history.overflow", findLocale(event.player.locale)));

            int i = 0;
            for(HistoryEntry entry : entries){
                message.append("\n").append(entry.getMessage(event.player));
                if(++i > 6) break;
            }

            if(entries.isEmpty()) message.append(Bundle.get("events.history.empty", findLocale(event.player.locale)));
       
            event.player.sendMessage(message.toString());
        }
    }
}
