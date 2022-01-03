package pandorum.commands.server;

import arc.util.Log;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Packets.KickReason;

import static mindustry.Vars.netServer;
import static pandorum.Misc.findPlayer;
import static pandorum.Misc.sendToChat;

public class BanCommand {
    public static void run(final String[] args) {
        switch (args[0].toLowerCase()) {
            case "id", "uuid" -> {
                netServer.admins.banPlayerID(args[1]);
                Log.info("Игрок успешно послан нахуй.");
            }
            case "name", "username" -> {
                Player target = findPlayer(args[1]);
                if (target != null) {
                    netServer.admins.banPlayer(target.uuid());
                    target.kick(KickReason.banned);
                    sendToChat("events.server.ban", target.coloredName());
                    Log.info("Игрок успешно послан нахуй.");
                } else {
                    Log.err("Игрок не найден...");
                }
                return;
            }
            case "ip", "address" -> {
                netServer.admins.banPlayerIP(args[1]);
                Log.info("Игрок успешно послан нахуй.");
            }
            default -> {
                Log.err("Неверный тип целеуказания бана. Выбери один из этих: id, name, ip");
                return;
            }
        }

        Groups.player.each(p -> netServer.admins.isIDBanned(p.uuid()) || netServer.admins.isIPBanned(p.ip()), p -> {
            p.kick(KickReason.banned);
            sendToChat("events.server.ban", p.coloredName());
        });
    }
}
