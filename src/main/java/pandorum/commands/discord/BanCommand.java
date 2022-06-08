package pandorum.commands.discord;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;
import pandorum.components.Bundle;
import pandorum.discord.Context;
import pandorum.util.Utils;

import static mindustry.Vars.netServer;
import static pandorum.PluginVars.discordServerUrl;
import static pandorum.util.Search.findLocale;
import static pandorum.util.Search.findPlayer;

public class BanCommand implements CommandRunner<Context> {

    @Override
    public void accept(String[] args, Context context) {
        if (!Utils.isAdmin(context.member)) {
            context.err(":no_entry_sign: Эта команда только для администрации.", "У тебя нет прав на ее использование.");
            return;
        }

        Player target = findPlayer(args[1]);
        if (target == null) {
            context.err(":mag: Игрок не найден.", "Проверь, правильно ли введен никнейм.");
            return;
        }

        netServer.admins.banPlayer(target.uuid());
        target.kick(Bundle.format("kick.banned", findLocale(target.locale), discordServerUrl), 0);
        context.info(":dagger: Игрок успешно забанен.", "@ больше не сможет зайти на сервер.", target.name);
        Utils.sendToChat("events.server.ban", target.coloredName());
    }
}
