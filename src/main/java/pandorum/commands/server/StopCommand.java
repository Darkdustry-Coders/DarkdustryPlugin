package pandorum.commands.server;

import arc.util.Log;
import mindustry.core.GameState.State;

import static mindustry.Vars.net;
import static mindustry.Vars.state;

public class StopCommand {
    public static void run(final String[] args) {
        Log.info("Сервер сдыхает...");
        net.closeServer();
        state.set(State.menu);
        Log.info("Сервер сдох.");
    }
}
