package pandorum.commands.discord.administration;

import arc.util.CommandHandler.CommandRunner;
import arc.util.Log;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.components.Bundle;
import pandorum.discord.Context;
import pandorum.util.Utils;

import static mindustry.Vars.netServer;
import static mindustry.Vars.state;
import static pandorum.PluginVars.discordServerUrl;
import static pandorum.util.Search.findLocale;
import static pandorum.util.Search.findPlayer;

public class BanCommand implements CommandRunner<Context> {

    @Override
    public void accept(String[] args, Context context) {
        Player target = findPlayer(args[0]);

        if (!Utils.isAdmin(context.member)) {
            context.err(":no_entry_sign: Эта команда только для администрации.", "У тебя нет прав на ее использование.");
            return;
        }

        if (state.isMenu()) {
            context.err(":gear: Сервер не запущен.", ":thinking: Почему?");
            return;
        }

        if (target == null) {
            Log.err("Игрок '@' не найден...", args[0]);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "id", "uuid" -> {
                netServer.admins.banPlayerID(args[1]);
                Log.info("Игрок '@' успешно забанен.", args[1]);
            }
            case "name", "username" -> {
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
                context.err("Неверный тип целеуказания бана. Выбери один из этих: id, name, ip");
                return;
            }
        }

        Groups.player.each(player -> netServer.admins.isIDBanned(player.uuid()) || netServer.admins.isIPBanned(player.ip()), player -> {
            player.kick(Bundle.format("kick.banned", findLocale(player.locale), discordServerUrl), 0);
            Utils.sendToChat("events.server.ban", player.coloredName());
        });

        context.info(":skull: Игрок успешно забанен на сервере.");
    }
}
