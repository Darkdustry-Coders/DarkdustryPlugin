package pandorum.commands.server;

import arc.util.Log;
import mindustry.gen.Player;
import pandorum.comp.Bundle;
import pandorum.util.Utils;

import static pandorum.PluginVars.discordServerUrl;
import static pandorum.PluginVars.kickDuration;
import static pandorum.util.Search.findLocale;
import static pandorum.util.Search.findPlayer;

public class KickCommand {
    public static void run(final String[] args) {
        Player target = findPlayer(args[0]);
        if (target == null) {
            Log.err("Игрок '@' не найден...", args[0]);
            return;
        }

        target.kick(Bundle.format("kick.kicked", findLocale(target.locale), Utils.millisecondsToMinutes(kickDuration), discordServerUrl), kickDuration);
        Log.info("Игрок '@' был выгнан с сервера.", target.name);
        Utils.sendToChat("events.server.kick", target.coloredName());
    }
}
