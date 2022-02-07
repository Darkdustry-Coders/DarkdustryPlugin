package pandorum.commands.client;

import mindustry.gen.Player;
import pandorum.components.Bundle;
import pandorum.util.Utils;

import static pandorum.util.Search.findLocale;

public class TeamChatCommand {
    public static void run(final String[] args, final Player player) {
        Utils.eachPlayerInTeam(player.team(), p -> p.sendMessage(Bundle.format("commands.t.chat", findLocale(p.locale), player.team().color, player.coloredName(), args[0]), player, args[0]));
    }
}
