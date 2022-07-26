package pandorum.commands.client;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.CommandHandler.Command;
import arc.util.CommandHandler.CommandRunner;
import arc.util.Strings;
import mindustry.gen.Player;
import pandorum.components.Bundle;

import static pandorum.PluginVars.clientCommands;
import static pandorum.util.PlayerUtils.bundled;
import static pandorum.util.Search.findLocale;

public class HelpCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (args.length > 0 && !Strings.canParseInt(args[0])) {
            bundled(player, "commands.page-not-int");
            return;
        }

        Seq<Command> commandsList = clientCommands.getCommandList();
        int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
        int pages = Mathf.ceil(commandsList.size / 8f);

        if (--page >= pages || page < 0) {
            bundled(player, "commands.under-page", pages);
            return;
        }

        StringBuilder result = new StringBuilder(Bundle.format("commands.help.page", findLocale(player.locale), page + 1, pages));

        for (int i = 8 * page; i < Math.min(8 * (page + 1), commandsList.size); i++) {
            Command command = commandsList.get(i);
            String paramText = Bundle.get("commands." + command.text + ".params", findLocale(player.locale), command.paramText);
            String description = Bundle.get("commands." + command.text + ".description", findLocale(player.locale), command.description);
            result.append("\n[orange] ").append(clientCommands.getPrefix()).append(command.text).append("[white] ").append(paramText).append("[lightgray] - ").append(description);
        }

        player.sendMessage(result.toString());
    }
}
