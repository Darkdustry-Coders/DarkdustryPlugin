package pandorum.commands.discord;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;
import pandorum.discord.Bot;
import pandorum.discord.MessageContext;

import static pandorum.PluginVars.kickDuration;
import static pandorum.util.PlayerUtils.*;
import static pandorum.util.Search.findPlayer;
import static pandorum.util.Utils.formatDuration;

public class KickCommand implements CommandRunner<MessageContext> {

    @Override
    public void accept(String[] args, MessageContext context) {
        if (!Bot.isAdmin(context.member)) {
            context.err(":no_entry_sign: Эта команда только для администрации.", "У тебя нет прав на ее использование.");
            return;
        }

        Player target = findPlayer(args[0]);
        if (target == null) {
            context.err(":mag: Игрок не найден.", "Проверь, правильно ли введен никнейм.");
            return;
        }

        kick(target, kickDuration, true, "kick.kicked");
        context.info(":skull: Игрок успешно выгнан с сервера.", "@ не сможет зайти на сервер в течение @", target.name, formatDuration(kickDuration));
        sendToChat("events.server.kick", target.name);
    }
}
