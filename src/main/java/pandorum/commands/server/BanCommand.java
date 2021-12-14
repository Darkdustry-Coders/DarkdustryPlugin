package pandorum.commands.server;

import arc.util.Log;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Packets.KickReason;
import pandorum.Misc;

import static mindustry.Vars.netServer;
import static pandorum.Misc.sendToChat;

public class BanCommand {
    public static void run(final String[] args) {
        Player target;
        switch (args[0].toLowerCase()) {
            case "id" -> {
                netServer.admins.banPlayerID(args[1]);
                target = Groups.player.find(p -> p.uuid().equals(args[1]));
                if (target != null) {
                    target.kick(KickReason.banned);
                    sendToChat("events.server.ban", target.coloredName());
                }
                Log.info("Игрок успешно забанен.");
            }
            case "name" -> {
                target = Misc.findByName(args[1]);
                if (target != null) {
                    netServer.admins.banPlayer(target.uuid());
                    target.kick(KickReason.banned);
                    sendToChat("events.server.ban", target.coloredName());
                    Log.info("Игрок успешно забанен.");
                } else {
                    Log.err("Игрок не найден...");
                }
            }
            case "ip" -> {
                netServer.admins.banPlayerIP(args[1]);
                target = Groups.player.find(p -> p.ip().equals(args[1]));
                if (target != null) {
                    target.kick(KickReason.banned);
                    sendToChat("events.server.ban", target.coloredName());
                }
                Log.info("Игрок успешно забанен.");
            }
            default -> Log.err("Неверный тип целеуказания бана.");
        }
    }
}
