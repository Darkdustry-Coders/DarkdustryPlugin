package pandorum.events;

import mindustry.game.EventType;
import mindustry.gen.Groups;
import net.dv8tion.jda.api.EmbedBuilder;
import org.bson.Document;
import pandorum.PandorumPlugin;
import pandorum.comp.Config.Gamemode;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

public class GameOverListener {
    public static void call(final EventType.GameOverEvent event) {
        Groups.player.each(p -> {
            Document playerInfo = PandorumPlugin.createInfo(p);
            int gamesPlayed = playerInfo.getInteger("gamesPlayed") + 1;
            playerInfo.replace("gamesPlayed", gamesPlayed);
            PandorumPlugin.savePlayerStats(p.uuid());
        });

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(BotMain.normalColor)
                .setAuthor("Сервер")
                .setTitle("Игра окончена! Загружаю новую карту!");

        BotHandler.botChannel.sendMessageEmbeds(embed.build()).queue();

        if (PandorumPlugin.config.mode == Gamemode.pvp || PandorumPlugin.config.mode == Gamemode.siege) PandorumPlugin.surrendered.clear();
        PandorumPlugin.votesRTV.clear();
        PandorumPlugin.votesVNW.clear();
    }
}
