package rewrite.commands;

import arc.func.Boolp;
import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;
import rewrite.components.Config.Gamemode;

import static rewrite.PluginVars.*;

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

    }, () -> config.mode != Gamemode.hub),
    surrender((args, player) -> {

    }, () -> config.mode == Gamemode.pvp),
    rtv((args, player) -> {

    }, config.mode::isDefault),
    vnw((args, player) -> {

    }, config.mode::isDefault),
    history((args, player) -> {

    }, config.mode::isDefault),
    allerts((args, player) -> {

    }, config.mode::isDefault),
    maps((args, player) -> {

    }, config.mode::isDefault),
    saves((args, player) -> {

    }, config.mode::isDefault),
    nominate((args, player) -> {

    }, config.mode::isDefault),
    voting((args, player) -> {

    }, config.mode::isDefault),
    artv((args, player) -> {

    }, config.mode::isDefault),
    despawn((args, player) -> {

    }, config.mode::isDefault),
    core((args, player) -> {

    }, config.mode::isDefault),
    give((args, player) -> {

    }, config.mode::isDefault),
    spawn((args, player) -> {

    }, config.mode::isDefault),
    team((args, player) -> {

    }, config.mode::isDefault),
    unit((args, player) -> {

    }, config.mode::isDefault),
    spectate((args, player) -> {

    }, config.mode::isDefault),
    tp((args, player) -> {

    }, config.mode::isDefault),
    fill((args, player) -> {

    }, config.mode::isDefault);

    public final String description;
    public final String params;
    private final CommandRunner<Player> runner;
    private final Boolp enabled;

    ClientCommands(CommandRunner<Player> runner) {
        this(runner, () -> true);
    }

    ClientCommands(CommandRunner<Player> runner, Boolp enabled) {
        this.description = "commands." + name() + ".desc";
        this.params = "commands." + name() + ".desc";
        this.runner = runner;
        this.enabled = enabled;
    }

    @Override
    public void accept(String[] args, Player parameter) {
        runner.accept(args, parameter);
    }

    public boolean enabled() {
        return enabled.get();
    }
}