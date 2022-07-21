package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;
import pandorum.components.Bundle;

import static pandorum.util.Search.findLocale;

public class TeamChatCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        player.team().data().players.each(p -> p.sendMessage(Bundle.format("commands.t.chat", findLocale(p.locale), player.team().color, player.name, args[0]), player, args[0]));
    }
}
