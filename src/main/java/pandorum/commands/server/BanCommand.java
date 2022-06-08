package pandorum.commands.server;

import arc.func.Cons;
import arc.util.Log;
import mindustry.gen.Groups;
import mindustry.gen.Player;

import static mindustry.Vars.netServer;
import static pandorum.util.PlayerUtils.kick;
import static pandorum.util.PlayerUtils.sendToChat;
import static pandorum.util.Search.findPlayer;

public class BanCommand implements Cons<String[]> {
    public void get(String[] args) {
        switch (args[0].toLowerCase()) {
            case "id", "uuid" -> {
                netServer.admins.banPlayerID(args[1]);
                Log.info("Игрок '@' успешно забанен.", args[1]);
            }
            case "ip", "address" -> {
                netServer.admins.banPlayerIP(args[1]);
                Log.info("Игрок '@' успешно забанен.", args[1]);
            }
            case "name", "username" -> {
                Player target = findPlayer(args[1]);
                if (target != null) {
                    netServer.admins.banPlayer(target.uuid());
                    kick(target, 0, true, "kick.banned");
                    Log.info("Игрок '@' успешно забанен.", target.name);
                    sendToChat("events.server.ban", target.coloredName());
                } else {
                    Log.err("Игрок '@' не найден...", args[1]);
                }
                return;
            }
            default -> {
                Log.err("Неверный тип целеуказания бана. Выбери один из этих: id, name, ip");
                return;
            }
        }

        Groups.player.each(player -> netServer.admins.isIDBanned(player.uuid()) || netServer.admins.isIPBanned(player.ip()), player -> {
            kick(player, 0, true, "kick.banned");
            sendToChat("events.server.ban", player.coloredName());
        });
    }
}
