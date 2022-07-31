package rewrite.commands;

import arc.func.Func;
import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;
import rewrite.components.Config.Gamemode;

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

    }),
    hub((args, player) -> {

    }, mode -> mode != Gamemode.hub),
    surrender((args, player) -> {

    }, mode -> mode == Gamemode.pvp),
    rtv((args, player) -> {

    }, Gamemode::isDefault),
    vnw((args, player) -> {

    }, Gamemode::isDefault),
    history((args, player) -> {

    }, Gamemode::isDefault),
    allerts((args, player) -> {

    }, Gamemode::isDefault),
    maps((args, player) -> {

    }, Gamemode::isDefault),
    saves((args, player) -> {

    }, Gamemode::isDefault),
    nominate((args, player) -> {

    }, Gamemode::isDefault),
    voting((args, player) -> {

    }, Gamemode::isDefault),
    artv((args, player) -> {

    }, Gamemode::isDefault),
    despawn((args, player) -> {

    }, Gamemode::isDefault),
    core((args, player) -> {

    }, Gamemode::isDefault),
    give((args, player) -> {

    }, Gamemode::isDefault),
    spawn((args, player) -> {

    }, Gamemode::isDefault),
    team((args, player) -> {

    }, Gamemode::isDefault),
    unit((args, player) -> {

    }, Gamemode::isDefault),
    spectate((args, player) -> {

    }, Gamemode::isDefault),
    tp((args, player) -> {

    }, Gamemode::isDefault),
    fill((args, player) -> {

    }, Gamemode::isDefault);

    public final String description;
    public final String params;
    private final CommandRunner<Player> runner;
    private final Func<Gamemode, Boolean> allowed;

    ClientCommands(CommandRunner<Player> runner) {
        this(runner, mode -> true);
    }

    ClientCommands(CommandRunner<Player> runner, Func<Gamemode, Boolean> allowed) {
        this.description = "commands." + name() + ".desc";
        this.params = "commands." + name() + ".desc";
        this.runner = runner;
        this.allowed = allowed;
    }

    @Override
    public void accept(String[] args, Player parameter) {
        runner.accept(args, parameter);
    }

    public boolean allowed(Gamemode mode) {
        return allowed.get(mode);
    }
}