package rewrite.commands;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;

public enum ClientCommands implements CommandRunner<Player> {
    adminChat((args, player) -> {

    }),
    alerts((args, player) -> {

    });

    private CommandRunner<Player> runner;

    private ClientCommands(CommandRunner<Player> runner) {
        this.runner = runner;

        // пример
        // for (ClientCommands command : ClientCommands.values()) {
        // handler.register(command.name(), command.desk(), )
        // }
    }

    public String name() {
        return name() + ".name";
    }

    public String desc() {
        return name + ".desc";
    }

    @Override
    public void accept(String[] args, Player parameter) {
        runner.accept(args, parameter);
    }
}