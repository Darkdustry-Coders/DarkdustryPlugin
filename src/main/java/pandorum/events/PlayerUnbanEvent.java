package pandorum.events;

import mindustry.game.EventType;
import mindustry.net.Administration.*;
import mindustry.Vars;
import pandorum.comp.*;
import pandorum.PandorumPlugin;

import java.awt.Color;

public class PlayerUnbanEvent {
    public static void call(final EventType.PlayerUnbanEvent event) {
        PlayerInfo info = Vars.netServer.admins.getInfo(event.uuid);
        if (info == null) return;
        DiscordSender.send("Сервер", "Игрок был разбанен!", "Никнейм:", info.lastName, "IP:", info.lastIP, new Color(255, 0, 0));
    }
}
