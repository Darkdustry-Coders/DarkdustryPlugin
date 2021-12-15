package pandorum.commands.client;

import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.annotations.commands.ClientCommand;
import pandorum.annotations.commands.OverrideCommand;
import pandorum.comp.Bundle;

import static pandorum.Misc.findLocale;

public class TeamChatCommand {
    @OverrideCommand
    @ClientCommand(name = "t", args = "<message...>", description = "Send message to teammates")
    public static void run(final String[] args, final Player player) {
        Groups.player.each(p -> p.team() == player.team(), p -> p.sendMessage(Bundle.format("commands.t.chat", findLocale(p.locale), player.team().color, player.coloredName(), args[0]), player, args[0]));
    }
}
