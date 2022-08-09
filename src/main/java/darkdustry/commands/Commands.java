package darkdustry.commands;

import arc.func.Cons;
import arc.util.CommandHandler;
import arc.util.CommandHandler.CommandRunner;

public class Commands<T> {

    public CommandHandler handler;

    public Commands(CommandHandler handler) {
        this.handler = handler;
    }

    public void register(String name, String params, String description, CommandRunner<T> runner) {

        //String params = get("commands." + name + ".params", locale);
        handler.register(name, params, description, runner);
    }

    public void register(String name, String description, CommandRunner<T> runner) {
        register(name, "", description, runner);
    }


    public void register(String name, String params, String description, Cons<String[]> runner) {
        handler.register(name, params, description, runner);
    }

    public void register(String name, String description, Cons<String[]> runner) {
        register(name, "", description, runner);
    }
}
