package pandorum.events.listeners;

import pandorum.database.models.MapModel;

import static mindustry.Vars.state;

public class WaveListener {

    public static void call() {
        MapModel.find(state.map, mapModel -> {
            mapModel.bestWave = Math.max(mapModel.bestWave, state.wave);
            mapModel.save();
        });
    }
}
