package darkdustry.commands;

import arc.struct.Seq;
import arc.util.CommandHandler;
import mindustry.Vars;

public class ModCommandHandler extends CommandHandler {
    private final CommandHandler proxied;

    public static void load() {
        Vars.netServer.clientCommands = new ModCommandHandler(Vars.netServer.clientCommands);
    }

    public ModCommandHandler(CommandHandler proxied) {
        super(proxied.prefix);
        this.proxied = proxied;
    }

    @Override
    public CommandResponse handleMessage(String message, Object params) {
        if (message.contains(" ")) {
            message = message.substring(0, message.indexOf(" ")).toLowerCase() + message.substring(message.indexOf(" "));
        }
        else {
            message = message.toLowerCase();
        }
        return proxied.handleMessage(message, params);
    }

    @Override
    public <T> Command register(String text, String params, String description, CommandRunner<T> runner) {
        return proxied.register(text, params, description, runner);
    }

    @Override
    public Seq<Command> getCommandList() {
        return proxied.getCommandList();
    }
}
