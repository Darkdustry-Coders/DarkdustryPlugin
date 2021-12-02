package pandorum.commands.client;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.CommandHandler.Command;
import arc.util.Strings;
import mindustry.gen.Player;
import pandorum.comp.Bundle;

import static mindustry.Vars.netServer;
import static pandorum.Misc.bundled;
import static pandorum.Misc.findLocale;

public class HelpCommand {

    private static final Seq<String> adminOnlyCommands = Seq.with("a", "artv", "ban", "changeunit", "core", "fill", "give", "spawn", "spectate", "team", "unban", "despw");

    public static void run(final String[] args, final Player player) {
        if (args.length > 0 && !Strings.canParseInt(args[0])) {
            bundled(player, "commands.page-not-int");
            return;
        }

        Seq<Command> commandList = player.admin ? netServer.clientCommands.getCommandList() : netServer.clientCommands.getCommandList().removeAll(command -> adminOnlyCommands.contains(command.text));
        int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
        int pages = Mathf.ceil(commandList.size / 6.0f);

        if (--page >= pages || page < 0) {
            bundled(player, "commands.under-page", pages);
            return;
        }

        StringBuilder result = new StringBuilder();
        result.append(Bundle.format("commands.help.page", findLocale(player.locale), page + 1, pages)).append("\n");

        for (int i = 6 * page; i < Math.min(6 * (page + 1), commandList.size); i++) {
            Command command = commandList.get(i);
            String description = Bundle.get(Strings.format("commands.@.description", command.text), findLocale(player.locale), command.description);
            result.append("[orange] /").append(command.text).append("[white] ").append(command.paramText).append("[lightgray] - ").append(description).append("\n");
        }
        player.sendMessage(result.toString());
    }
}
