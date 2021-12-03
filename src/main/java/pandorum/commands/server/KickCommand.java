package pandorum.commands.server;

import arc.util.Log;
import mindustry.gen.Player;
import mindustry.net.Packets.KickReason;
import pandorum.Misc;

import static pandorum.Misc.sendToChat;

public class KickCommand {
    public static void run(final String[] args) {
        Player target = Misc.findByName(args[0]);

        if (target != null) {
            target.kick(KickReason.kick);
            sendToChat("events.server.kick", target.coloredName());
            Log.info("Игрок был успешно выгнан.");
        } else {
            Log.err("Игрок не найден...");
        }
    }
}
