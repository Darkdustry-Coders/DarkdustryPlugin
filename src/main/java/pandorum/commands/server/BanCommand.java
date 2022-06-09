package pandorum.commands.server;

import arc.func.Cons;
import arc.util.Log;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration.PlayerInfo;

import static mindustry.Vars.netServer;
import static pandorum.util.PlayerUtils.kick;
import static pandorum.util.PlayerUtils.sendToChat;
import static pandorum.util.Search.findPlayer;

public class BanCommand implements Cons<String[]> {
    public void get(String[] args) {
        Player target = findPlayer(args[0]);
        if (target != null) {
            netServer.admins.banPlayer(target.uuid());
            kick(target, 0, true, "kick.banned");
            Log.info("Игрок '@' успешно забанен.", target.name);
            sendToChat("events.server.ban", target.coloredName());
            return;
        }

        PlayerInfo info;
        if ((info = netServer.admins.getInfoOptional(args[0])) == null && (info = netServer.admins.findByIP(args[0])) == null) {
            Log.err("Игрок '@' не найден...", args[0]);
            return;
        }

        netServer.admins.banPlayer(info.id);
        Log.info("Игрок '@' успешно забанен.", args[0]);

        Groups.player.each(player -> netServer.admins.isIDBanned(player.uuid()) || netServer.admins.isIPBanned(player.ip()), player -> {
            kick(player, 0, true, "kick.banned");
            sendToChat("events.server.ban", player.coloredName());
        });
    }
}
