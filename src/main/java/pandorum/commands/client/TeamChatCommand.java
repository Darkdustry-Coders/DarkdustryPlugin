package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;
import pandorum.components.Bundle;
import pandorum.util.PlayerUtils;

import static pandorum.util.Search.findLocale;

public class TeamChatCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        PlayerUtils.eachPlayer(player.team(), p -> p.sendMessage(Bundle.format("commands.t.chat", findLocale(p.locale), player.team().color, player.name, args[0]), player, args[0]));
    }
}
