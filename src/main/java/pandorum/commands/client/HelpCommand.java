package pandorum.commands.client;

import arc.math.Mathf;
import arc.util.CommandHandler;
import arc.util.Strings;
import mindustry.gen.Player;
import pandorum.comp.Bundle;

import static mindustry.Vars.netServer;
import static pandorum.Misc.bundled;
import static pandorum.Misc.findLocale;

public class HelpCommand implements ClientCommand {
    public static void run(final String[] args, final Player player) {
        if (args.length > 0 && !Strings.canParseInt(args[0])) {
            bundled(player, "commands.page-not-int");
            return;
        }
        int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
        int pages = Mathf.ceil(netServer.clientCommands.getCommandList().size / 6.0f);

        if (--page >= pages || page < 0) {
            bundled(player, "commands.under-page", String.valueOf(pages));
            return;
        }

        StringBuilder result = new StringBuilder();
        result.append(Bundle.format("commands.help.page", findLocale(player.locale), page + 1, pages)).append("\n");

        for (int i = 6 * page; i < Math.min(6 * (page + 1), netServer.clientCommands.getCommandList().size); i++) {
            CommandHandler.Command command = netServer.clientCommands.getCommandList().get(i);
            String desc = Bundle.has(Strings.format("commands.@.description", command.text), findLocale(player.locale)) ? Bundle.format(Strings.format("commands.@.description", command.text), findLocale(player.locale)) : command.description;
            result.append("[orange] /").append(command.text).append("[white] ").append(command.paramText).append("[lightgray] - ").append(desc).append("\n");
        }
        player.sendMessage(result.toString());
    }
}
