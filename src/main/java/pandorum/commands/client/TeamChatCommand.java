package pandorum.commands.client;

import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.Misc;

import static pandorum.Misc.bundled;

public class TeamChatCommand {
    public static void run(final String[] args, final Player player) {
        Groups.player.each(p -> p.team() == player.team(), teammate -> bundled(teammate, "commands.t.chat", player.team().color, Misc.colorizedName(player), args[0]));
    }
}
