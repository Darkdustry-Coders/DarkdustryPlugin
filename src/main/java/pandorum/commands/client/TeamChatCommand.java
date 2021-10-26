package pandorum.commands.client;

import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.Misc;
import pandorum.comp.Bundle;

import static pandorum.Misc.findLocale;

public class TeamChatCommand implements ClientCommand {
    public static void run(final String[] args, final Player player) {
        Groups.player.each(p -> p.team() == player.team(), p -> p.sendMessage(Bundle.format("commands.t.chat", findLocale(p.locale), player.team().color, Misc.colorizedName(player), args[0]), player, args[0]));
    }
}
