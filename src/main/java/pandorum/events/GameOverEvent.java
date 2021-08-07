package pandorum.events;

import mindustry.game.EventType;
import pandorum.PandorumPlugin;

public class GameOverEvent {
    public static void call(final EventType.GameOverEvent event) {
        if(PandorumPlugin.config.type == PluginType.other) return;
        else if(PandorumPlugin.config.type == PluginType.pvp) surrendered.clear();
        votesRTV.clear();
        votesVNW.clear();
    }
}
