package pandorum.commands;

import arc.func.Cons;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.CommandHandler.Command;
import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;
import pandorum.Misc;
import pandorum.PandorumPlugin;
import pandorum.comp.Config.Gamemode;

public class CommandsHelper {

    public static Seq<Command> adminOnlyCommands = new Seq<>();

    public static void register(CommandHandler clientHandler, String text, String params, String description, boolean adminOnly, Seq<Gamemode> modes, CommandRunner<Player> runner) {
        if (!modes.contains(PandorumPlugin.config.mode)) return;
        Command command = clientHandler.<Player>register(text, params, description, (args, player) -> {
            if (adminOnly && Misc.adminCheck(player)) return;
            runner.accept(args, player);
        });

        if (adminOnly) adminOnlyCommands.add(command);
    }

    public static void register(CommandHandler clientHandler, String text, String params, String description, boolean adminOnly, CommandRunner<Player> runner) {
        register(clientHandler, text, params, description, adminOnly, Seq.with(Gamemode.values()), runner);
    }

    public static void register(CommandHandler clientHandler, String text, String description, boolean adminOnly, Seq<Gamemode> modes, CommandRunner<Player> runner) {
        register(clientHandler, text, "", description, adminOnly, modes, runner);
    }

    public static void register(CommandHandler clientHandler, String text, String description, boolean adminOnly, CommandRunner<Player> runner) {
        register(clientHandler, text, "", description, adminOnly, runner);
    }

    public static void register(CommandHandler serverHandler, String text, String params, String description, Cons<String[]> runner) {
        serverHandler.register(text, params, description, runner);
    }

    public static void register(CommandHandler serverHandler, String text, String description, Cons<String[]> runner) {
        register(serverHandler, text, "", description, runner);
    }
}
