package rewrite.commands;

import arc.util.CommandHandler.CommandRunner;
import pandorum.discord.MessageContext;

public enum DiscordCommands implements CommandRunner<MessageContext> {
    help("Список всех команд.", (args, context) -> {

    }),
    ip("IP адрес сервера.", (args, context) -> {

    }),
    players("Список всех игроков на сервере.", "[страница]", (args, context) -> {

    }),
    status("Состояние сервера.", (args, context) -> {

    }),
    kick("Выгнать игрока с сервера.", "<ID/никнейм...>", (args, context) -> {

    }),
    ban("Забанить игрока на сервере.", "<ID/никнейм...>", (args, context) -> {

    });

    public final String description;
    public final String params;
    private final CommandRunner<MessageContext> runner;

    DiscordCommands(String description, CommandRunner<MessageContext> runner) {
        this(description, "", runner);
    }

    DiscordCommands(String description, String params, CommandRunner<MessageContext> runner) {
        this.description = description;
        this.params = params;
        this.runner = runner;
    }

    @Override
    public void accept(String[] args, MessageContext context) {
        runner.accept(args, context);
    }
}