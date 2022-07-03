package pandorum.commands.server;

import arc.func.Cons;
import arc.util.Log;
import mindustry.core.GameState.State;

import static mindustry.Vars.*;

public class StopCommand implements Cons<String[]> {
    public void get(String[] args) {
        net.closeServer();
        state.set(State.menu);
        Log.info("Сервер остановлен.");
    }
}
