package pandorum.commands.discord.administration;

import arc.util.CommandHandler.CommandRunner;
import arc.util.Log;
import mindustry.gen.Player;
import pandorum.components.Bundle;
import pandorum.discord.Context;
import pandorum.util.Utils;

import static mindustry.Vars.state;
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

        if (state.isMenu()) {
            context.err(":gear: Сервер не запущен.", ":thinking: Почему?");
            return;
        }

        Player target = findPlayer(args[0]);
        if (target == null) {
            Log.err("Игрок '@' не найден...", args[0]);
            return;
        }

        target.kick(Bundle.format("kick.kicked", findLocale(target.locale), Utils.millisecondsToMinutes(kickDuration), discordServerUrl), kickDuration);
        Log.info("Игрок '@' был выгнан с сервера.", target.name);
        Utils.sendToChat("events.server.kick", target.coloredName());

        context.info(":skull: Игрок успешно выгнан с сервера.");
    }
}
