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

    public String desc;
    public String params;
    private CommandRunner<MessageContext> runner;

    private DiscordCommands(String desc, CommandRunner<MessageContext> runner) {
        this(desc, "", runner);
    }

    private DiscordCommands(String desc, String params, CommandRunner<MessageContext> runner) {
        this.desc = desc;
        this.params = params;
        this.runner = runner;
    }

    @Override
    public void accept(String[] args, MessageContext context) {
        runner.accept(args, context);
    }
}