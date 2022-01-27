package pandorum.events.listeners;

import mindustry.game.EventType.TapEvent;
import pandorum.comp.Bundle;
import pandorum.entry.CacheEntry;

import static pandorum.Misc.findLocale;
import static pandorum.PluginVars.*;

public class TapListener {

    public static void call(final TapEvent event) {
        if (config.historyEnabled() && activeHistoryPlayers.contains(event.player.uuid()) && event.tile != null) {
            history.getAll(event.tile.x, event.tile.y, historyEntries -> {
                StringBuilder historyString = new StringBuilder(Bundle.format("history.title", findLocale(event.player.locale), event.tile.x, event.tile.y));

                for (CacheEntry entry : historyEntries)
                    historyString.append("\n").append(entry.getMessage(event.player));

                if (historyEntries.isEmpty())
                    historyString.append(Bundle.format("history.empty", findLocale(event.player.locale)));

                event.player.sendMessage(historyString.toString());
            });
        }
    }
}
