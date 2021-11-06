package pandorum.events;

import com.mongodb.BasicDBObject;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import pandorum.PandorumPlugin;
import pandorum.comp.Config.Gamemode;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;
import pandorum.models.PlayerModel;

public class GameOverListener {
    public static void call(final EventType.GameOverEvent event) {
        Groups.player.each(p -> PlayerModel.find(new BasicDBObject("UUID", p.uuid()), playerInfo -> {
            playerInfo.gamesPlayed++;
            playerInfo.save();
        }));

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(BotMain.normalColor)
                .setAuthor("Gameover")
                .setTitle("Игра окончена. Загружаю новую карту...");

        BotHandler.sendEmbed(embed);

        if (PandorumPlugin.config.mode == Gamemode.pvp || PandorumPlugin.config.mode == Gamemode.siege) PandorumPlugin.surrendered.clear();
        PandorumPlugin.votesRTV.clear();
        PandorumPlugin.votesVNW.clear();
    }
}
