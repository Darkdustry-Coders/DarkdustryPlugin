package pandorum.events;

import com.mongodb.BasicDBObject;
import discord4j.core.spec.EmbedCreateSpec;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import pandorum.PandorumPlugin;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;
import pandorum.models.PlayerModel;

public class GameOverListener {
    public static void call(final EventType.GameOverEvent event) {
        Groups.player.each(p -> PlayerModel.find(new BasicDBObject("UUID", p.uuid()), playerInfo -> {
            playerInfo.gamesPlayed++;
            playerInfo.save();
        }));

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(BotMain.normalColor)
                .author("Gameover", null, "https://w7.pngwing.com/pngs/679/718/png-transparent-globe-world-font-awesome-icon-globe-miscellaneous-map-earth-thumbnail.png")
                .title("Игра окончена. Загружаю новую карту...")
                .build();

        BotHandler.sendEmbed(embed);

        PandorumPlugin.surrendered.clear();
        PandorumPlugin.votesRTV.clear();
        PandorumPlugin.votesVNW.clear();
    }
}
