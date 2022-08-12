package darkdustry.commands;

import arc.util.CommandHandler;

/** Раньше в этом классе был смысл, но дарк - это дарк. */
public class Commands<T> {

    public CommandHandler handler;

    public Commands(CommandHandler handler) {
        this.handler = handler;
    }
}
