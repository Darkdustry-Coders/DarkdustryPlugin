package pandorum.commands.server;

import arc.Events;
import arc.func.Cons;
import arc.util.Log;
import mindustry.game.EventType.GameOverEvent;

import static mindustry.Vars.state;

public class GameOverCommand implements Cons<String[]> {
    public void get(String[] args) {
        if (state.isMenu()) {
            Log.err("Сервер отключен.");
            return;
        }

        Log.info("Завершаю игру.");
        Events.fire(new GameOverEvent(state.rules.waveTeam));
    }
}
