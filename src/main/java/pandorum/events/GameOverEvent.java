package pandorum.events;

import mindustry.game.EventType;
import pandorum.PandorumPlugin;
import pandorum.comp.Config.PluginType;
import pandorum.comp.*;

import java.io.IOException;
import java.awt.Color;

public class GameOverEvent {
    public static void call(final EventType.GameOverEvent event) {
        DiscordSender.send("Сервер", "Игра окончена!", new Color(0, 222, 222));

        if(PandorumPlugin.config.type == PluginType.other) return;
        else if(PandorumPlugin.config.type == PluginType.pvp) PandorumPlugin.surrendered.clear();
        PandorumPlugin.votesRTV.clear();
        PandorumPlugin.votesVNW.clear();
    }
}
