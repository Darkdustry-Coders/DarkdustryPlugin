package pandorum.commands.server;

import arc.util.Log;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.comp.Bundle;
import pandorum.utils.Utils;

import static mindustry.Vars.netServer;
import static pandorum.utils.Search.*;
import static pandorum.PluginVars.discordServerUrl;

public class BanCommand {
    public static void run(final String[] args) {
        switch (args[0].toLowerCase()) {
            case "id", "uuid" -> {
                netServer.admins.banPlayerID(args[1]);
                Log.info("Игрок '@' успешно забанен.", args[1]);
            }
            case "name", "username" -> {
                Player target = findPlayer(args[1]);
                if (target != null) {
                    netServer.admins.banPlayer(target.uuid());
                    target.kick(Bundle.format("kick.banned", findLocale(target.locale), discordServerUrl), 0);
                    Log.info("Игрок '@' успешно забанен.", target.name);
                    Utils.sendToChat("events.server.ban", target.coloredName());
                } else {
                    Log.err("Игрок '@' не найден...", args[1]);
                }
                return;
            }
            case "ip", "address" -> {
                netServer.admins.banPlayerIP(args[1]);
                Log.info("Игрок '@' успешно забанен.", args[1]);
            }
            default -> {
                Log.err("Неверный тип целеуказания бана. Выбери один из этих: id, name, ip");
                return;
            }
        }

        Groups.player.each(player -> netServer.admins.isIDBanned(player.uuid()) || netServer.admins.isIPBanned(player.ip()), player -> {
            player.kick(Bundle.format("kick.banned", findLocale(player.locale), discordServerUrl), 0);
            Utils.sendToChat("events.server.ban", player.coloredName());
        });
    }
}
