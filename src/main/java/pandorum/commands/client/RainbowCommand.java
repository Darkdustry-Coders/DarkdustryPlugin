package pandorum.commands.client;

import arc.util.Strings;
import mindustry.gen.Player;
import pandorum.Misc;
import pandorum.comp.RainbowPlayerEntry;

import static mindustry.Vars.netServer;
import static pandorum.Misc.bundled;
import static pandorum.PandorumPlugin.rainbow;

public class RainbowCommand {
    public static void run(final String[] args, final Player player) {

        RainbowPlayerEntry old = rainbow.find(entry -> entry.player.uuid().equals(player.uuid()));
        if (old != null) {
            rainbow.remove(old);
            player.name = netServer.admins.getInfo(player.uuid()).lastName;
            bundled(player, "commands.rainbow.off");
            return;
        }
        bundled(player, "commands.rainbow.on");
        RainbowPlayerEntry entry = new RainbowPlayerEntry();
        entry.player = player;
        entry.stripedName = Strings.stripColors(player.name);
        rainbow.add(entry);
    }
}
