package pandorum.events;

import arc.util.Strings;
import mindustry.game.EventType;
import mindustry.net.Administration.PlayerInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

import static mindustry.Vars.netServer;

public class PlayerBanListener {
    public static void call(final EventType.PlayerBanEvent event) {
        PlayerInfo info = netServer.admins.getInfo(event.uuid);
        if (info != null) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(BotMain.errorColor)
                    .setAuthor("Банхаммер")
                    .setTitle("Игрок был заблокирован!")
                    .addField("Никнейм:", Strings.stripColors(info.lastName), false)
                    .addField("IP:", info.lastIP, false)
                    .addField("Uuid:", event.uuid, false);

            BotHandler.botChannel.sendMessageEmbeds(embed.build()).queue();
        }
    }
}
