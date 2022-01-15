package pandorum.commands.server;

import arc.util.Log;
import mindustry.gen.Player;
import pandorum.comp.Bundle;

import static pandorum.Misc.*;
import static pandorum.PluginVars.discordServerUrl;
import static pandorum.PluginVars.kickDuration;

public class KickCommand {
    public static void run(final String[] args) {
        Player target = findPlayer(args[0]);
        if (target == null) {
            Log.err("Игрок '@' не найден...", args[0]);
            return;
        }

        target.kick(Bundle.format("kick.kicked", findLocale(target.locale), millisecondsToMinutes(kickDuration), discordServerUrl), kickDuration);
        Log.info("Игрок '@' был выгнан с сервера.", target.name);
        sendToChat("events.server.kick", target.coloredName());
    }
}
