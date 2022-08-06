package rewrite.commands;

import arc.func.Cons;
import arc.util.CommandHandler;
import arc.util.CommandHandler.CommandRunner;

import java.util.Locale;

import static rewrite.components.Bundle.get;

public class Commands<T> {

    public CommandHandler handler;
    public Locale locale;

    public Commands(CommandHandler handler, Locale locale) {
        this.handler = handler;
        this.locale = locale;
    }

    public void register(String name, CommandRunner<T> runner) {
        String params = get("commands." + name + ".params", locale);
        handler.register(name, params.startsWith("commands") ? "" : params, get("commands." + name + ".description", locale), runner);
    }

    public void register(String name, Cons<String[]> runner) {
        String params = get("commands." + name + ".params", locale);
        handler.register(name, params.startsWith("commands") ? "" : params, get("commands." + name + ".description", locale), runner);
    }
}
