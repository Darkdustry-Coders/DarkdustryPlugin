package pandorum.commands.server;

import arc.util.Log;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Packets.KickReason;
import pandorum.Misc;
import pandorum.annotations.commands.ServerCommand;

import static mindustry.Vars.netServer;
import static pandorum.Misc.sendToChat;

public class ConsoleBanCommand {
    @ServerCommand(name = "ban", args = "<ip/name/id> <ip/username/uuid...>", description = "Ban a player by ip, name or uuid.")
    public static void run(final String[] args) {
        switch (args[0].toLowerCase()) {
            case "id" -> {
                netServer.admins.banPlayerID(args[1]);
                Player target = Groups.player.find(p -> p.uuid().equals(args[1]));
                if (target != null) {
                    target.kick(KickReason.banned);
                    sendToChat("events.server.ban", target.coloredName());
                }
                Log.info("Игрок успешно забанен.");
            }
            case "name" -> {
                Player target = Misc.findByName(args[1]);
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
                Player target = Groups.player.find(p -> p.ip().equals(args[1]));
                if (target != null) {
                    target.kick(KickReason.banned);
                    sendToChat("events.server.ban", target.coloredName());
                }
                Log.info("Игрок успешно забанен.");
            }
            default -> Log.err("Неверный тип бана.");
        }
    }
}
