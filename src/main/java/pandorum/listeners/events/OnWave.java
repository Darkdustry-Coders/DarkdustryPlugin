package pandorum.listeners.events;

import arc.func.Cons;
import mindustry.game.EventType.WaveEvent;
import pandorum.mongo.models.MapModel;

import static mindustry.Vars.state;

public class OnWave implements Cons<WaveEvent> {

    public void get(WaveEvent event) {
        MapModel.find(state.map, mapModel -> {
            mapModel.bestWave = Math.max(mapModel.bestWave, state.wave);
            mapModel.save();
        });
    }
}
