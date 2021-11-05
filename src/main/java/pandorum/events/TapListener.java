package pandorum.events;

import mindustry.game.EventType;

import pandorum.PandorumPlugin;
import pandorum.comp.*;
import pandorum.entry.*;
import pandorum.struct.*;
import static pandorum.Misc.*;

public class TapListener {
    public static void call(final EventType.TapEvent event) {
        if (PandorumPlugin.activeHistoryPlayers.contains(event.player.uuid()) && event.tile != null) {
            CacheSeq<HistoryEntry> entries = PandorumPlugin.history[event.tile.x][event.tile.y];
            StringBuilder history = new StringBuilder(Bundle.format("history.title", findLocale(event.player.locale), event.tile.x, event.tile.y));

            entries.cleanUp();
            if (entries.isOverflown()) history.append(Bundle.get("history.overflow", findLocale(event.player.locale)));

            for (HistoryEntry entry : entries) {
                history.append("\n").append(entry.getMessage(event.player));
            }

            if (entries.isEmpty()) history.append(Bundle.get("history.empty", findLocale(event.player.locale)));
       
            event.player.sendMessage(history.toString());
        }
    }
}
