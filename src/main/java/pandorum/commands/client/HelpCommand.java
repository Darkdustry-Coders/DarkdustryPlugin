package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.CommandHandler.Command;
import arc.util.Strings;
import mindustry.gen.Player;
import pandorum.components.Bundle;
import pandorum.util.Utils;

import static pandorum.util.Search.findLocale;
import static pandorum.util.Utils.adminCheck;
import static pandorum.util.Utils.bundled;

public class HelpCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (args.length > 0 && !Strings.canParseInt(args[0])) {
            bundled(player, "commands.page-not-int");
            return;
        }

        Seq<Command> commandsList = Utils.getAvailableClientCommands(adminCheck(player));
        int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
        int pages = Mathf.ceil(commandsList.size / 8f);

        if (--page >= pages || page < 0) {
            bundled(player, "commands.under-page", pages);
            return;
        }

        StringBuilder result = new StringBuilder(Bundle.format("commands.help.page", findLocale(player.locale), page + 1, pages));

        for (int i = 8 * page; i < Math.min(8 * (page + 1), commandsList.size); i++) {
            Command command = commandsList.get(i);
            result.append("\n[orange] /").append(command.text).append("[white] ").append(command.paramText).append("[lightgray] - ").append(Bundle.getOrDefault(Strings.format("commands.@.description", command.text), command.description, findLocale(player.locale)));
        }

        player.sendMessage(result.toString());
    }
}
