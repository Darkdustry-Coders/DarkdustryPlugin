package pandorum.listeners.events;

import arc.func.Cons;
import mindustry.game.EventType.TapEvent;
import pandorum.components.Bundle;
import pandorum.features.history.HistorySeq;
import pandorum.features.history.entry.HistoryEntry;

import static pandorum.PluginVars.*;
import static pandorum.util.Search.findLocale;

public class OnTap implements Cons<TapEvent> {

    public void get(TapEvent event) {
        if (historyEnabled() && activeHistoryPlayers.contains(event.player.uuid()) && event.tile != null) {
            StringBuilder result = new StringBuilder(Bundle.format("history.title", findLocale(event.player.locale), event.tile.x, event.tile.y));
            HistorySeq entries = history[event.tile.x][event.tile.y];

            for (HistoryEntry entry : entries) {
                result.append("\n").append(entry.getMessage(event.player));
            }

            if (entries.isEmpty()) {
                result.append(Bundle.format("history.empty", findLocale(event.player.locale)));
            }

            event.player.sendMessage(result.toString());
        }
    }
}
