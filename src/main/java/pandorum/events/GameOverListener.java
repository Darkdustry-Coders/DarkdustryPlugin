package pandorum.events;

import mindustry.game.EventType;
import mindustry.gen.Groups;
import org.bson.Document;
import pandorum.PandorumPlugin;
import pandorum.comp.Config.Gamemode;

public class GameOverListener {
    public static void call(final EventType.GameOverEvent event) {
        Groups.player.each(p -> {
            Document playerInfo = PandorumPlugin.createInfo(p);
            int gamesPlayed = playerInfo.getInteger("gamesPlayed") + 1;
            playerInfo.replace("gamesPlayed", gamesPlayed);
            PandorumPlugin.savePlayerStats(p.uuid());
        });

        if (PandorumPlugin.config.mode == Gamemode.pvp || PandorumPlugin.config.mode == Gamemode.siege) PandorumPlugin.surrendered.clear();
        PandorumPlugin.votesRTV.clear();
        PandorumPlugin.votesVNW.clear();
    }
}
