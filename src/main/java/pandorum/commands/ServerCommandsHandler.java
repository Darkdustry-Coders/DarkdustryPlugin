package pandorum.commands;

import arc.Core;
import arc.func.Cons;
import arc.util.CommandHandler;

public class ServerCommandsHandler {

    public CommandHandler handler;

    public ServerCommandsHandler(CommandHandler handler) {
        this.handler = handler;
    }

    public void register(String text, String params, String description, Cons<String[]> runner) {
        handler.register(text, params, description, args -> Core.app.post(() -> runner.get(args)));
    }

    public void register(String text, String description, Cons<String[]> runner) {
        register(text, "", description, runner);
    }

    public void removeCommand(String text) {
        handler.removeCommand(text);
    }
}
