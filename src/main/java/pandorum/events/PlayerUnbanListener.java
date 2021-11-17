package pandorum.events;

import arc.util.Strings;
import discord4j.core.spec.EmbedCreateSpec;
import mindustry.game.EventType;
import mindustry.net.Administration.PlayerInfo;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

import static mindustry.Vars.netServer;

public class PlayerUnbanListener {
    public static void call(final EventType.PlayerUnbanEvent event) {
        PlayerInfo info = netServer.admins.getInfoOptional(event.uuid);
        if (info != null) {
            EmbedCreateSpec embed = EmbedCreateSpec.builder()
                    .color(BotMain.errorColor)
                    .author("UNBAN", null, "https://cdn.icon-icons.com/icons2/364/PNG/256/Banned_36950.png")
                    .title("Игрок был разбанен!")
                    .addField("Никнейм:", Strings.stripColors(info.lastName), false)
                    .addField("UUID:", event.uuid, false)
                    .build();

            BotHandler.sendEmbed(embed);
        }
    }
}
