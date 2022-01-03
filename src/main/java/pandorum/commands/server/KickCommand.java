package pandorum.commands.server;

import arc.util.Log;
import mindustry.gen.Player;
import mindustry.net.Packets.KickReason;

import static pandorum.Misc.findPlayer;
import static pandorum.Misc.sendToChat;

public class KickCommand {
    public static void run(final String[] args) {
        Player target = findPlayer(args[0]);
        if (target == null) {
            Log.err("Игрок '@' не найден...", args[0]);
            return;
        }

        target.kick(KickReason.kick);
        sendToChat("events.server.kick", target.coloredName());
        Log.info("Игрок '@' был выгнан с сервера.", target.name);
    }
}
