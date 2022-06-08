package pandorum.commands.discord;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;
import pandorum.components.Bundle;
import pandorum.discord.Context;
import pandorum.util.Utils;

import static pandorum.PluginVars.discordServerUrl;
import static pandorum.PluginVars.kickDuration;
import static pandorum.util.Search.findLocale;
import static pandorum.util.Search.findPlayer;

public class KickCommand implements CommandRunner<Context> {


    @Override
    public void accept(String[] args, Context context) {
        if (!Utils.isAdmin(context.member)) {
            context.err(":no_entry_sign: Эта команда только для администрации.", "У тебя нет прав на ее использование.");
            return;
        }

        Player target = findPlayer(args[0]);
        if (target == null) {
            context.err(":mag: Игрок не найден.", "Проверь, правильно ли введен никнейм.");
            return;
        }

        target.kick(Bundle.format("kick.kicked", findLocale(target.locale), Utils.millisecondsToMinutes(kickDuration), discordServerUrl), kickDuration);
        context.info(":skull: Игрок успешно выгнан с сервера.", "@ не сможет зайти на сервер в течение @ минут", target.name, Utils.millisecondsToMinutes(kickDuration));
        Utils.sendToChat("events.server.kick", target.coloredName());
    }
}
