package rewrite.commands;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;

public enum ClientCommands implements CommandRunner<Player> {
    adminChat((args, player) -> {

    }),
    alerts((args, player) -> {

    });

    private String name;
    private CommandRunner<Player> runner;

    private ClientCommands(String name, CommandRunner<Player> runner) {
        this.name = ""name;
        this.runner = runner;

        // пример
        // for (ClientCommands command : ClientCommands.values()) {
        // handler.register(command.name(), command.desk(), )
        // }
    }

    public String name() {
        return name + ".name";
    }

    public String desc() {
        return name + ".desc";
    }

    @Override
    public void accept(String[] args, Player parameter) {
        runner.accept(args, parameter);
    }
}