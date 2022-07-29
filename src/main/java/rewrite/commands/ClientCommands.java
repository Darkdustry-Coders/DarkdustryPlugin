package rewrite.commands;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;

public enum ClientCommands implements CommandRunner<Player> {
    help((args, player) -> {

    }),
    discord((args, player) -> {

    }),
    a((args, player) -> {

    }),
    t((args, player) -> {

    }),
    votekick((args, player) -> {

    }),
    vote((args, player) -> {

    }),
    sync((args, player) -> {

    }),
    tr((args, player) -> {

    }),
    start((args, player) -> {

    }),
    rank((args, player) -> {

    }),
    players((args, player) -> {

    }),
    login((args, player) -> {

    });

    public final String description;
    public final String params;
    private final CommandRunner<Player> runner;

    ClientCommands(CommandRunner<Player> runner) {
        this.description = "commands." + name() + ".desc";
        this.params = "commands." + name() + ".desc";
        this.runner = runner;
    }

    @Override
    public void accept(String[] args, Player parameter) {
        runner.accept(args, parameter);
    }
}