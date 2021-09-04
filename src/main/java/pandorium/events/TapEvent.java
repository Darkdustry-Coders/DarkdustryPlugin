package pandorium.events;

import mindustry.game.EventType;

import pandorium.PandorumPlugin;
import pandorium.comp.*;
import pandorium.entry.*;
import pandorium.struct.*;
import static pandorium.Misc.*;

public class TapEvent {
    public static void call(final EventType.TapEvent event) {
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