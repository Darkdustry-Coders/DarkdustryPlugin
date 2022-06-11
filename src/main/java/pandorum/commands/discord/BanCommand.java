package pandorum.commands.discord;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;
import pandorum.discord.Context;

import static mindustry.Vars.netServer;
import static pandorum.util.PlayerUtils.*;
import static pandorum.util.Search.findPlayer;

public class BanCommand implements CommandRunner<Context> {

    @Override
    public void accept(String[] args, Context context) {
        if (!isAdmin(context.member)) {
            context.err(":no_entry_sign: Эта команда только для администрации.", "У тебя нет прав на ее использование.");
            return;
        }

        Player target = findPlayer(args[0]);
        if (target == null) {
            context.err(":mag: Игрок не найден.", "Проверь, правильно ли введен никнейм.");
            return;
        }

        netServer.admins.banPlayer(target.uuid());
        kick(target, 0, true, "kick.banned");
        context.info(":dagger: Игрок успешно забанен.", "@ больше не сможет зайти на сервер.", target.name);
        sendToChat("events.server.ban", target.name);
    }
}
