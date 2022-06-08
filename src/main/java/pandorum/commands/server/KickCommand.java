package pandorum.commands.server;

import arc.func.Cons;
import arc.util.Log;
import mindustry.gen.Player;

import static pandorum.PluginVars.kickDuration;
import static pandorum.util.PlayerUtils.kick;
import static pandorum.util.PlayerUtils.sendToChat;
import static pandorum.util.Search.findPlayer;

public class KickCommand implements Cons<String[]> {
    public void get(String[] args) {
        Player target = findPlayer(args[0]);
        if (target == null) {
            Log.err("Игрок '@' не найден...", args[0]);
            return;
        }

        kick(target, kickDuration, true, "kick.kicked");
        Log.info("Игрок '@' был выгнан с сервера.", target.name);
        sendToChat("events.server.kick", target.coloredName());
    }
}
