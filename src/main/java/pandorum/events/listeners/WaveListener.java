package pandorum.events.listeners;

import pandorum.database.models.MapModel;

import static mindustry.Vars.state;

public class WaveListener implements Runnable {

    public void run() {
        MapModel.find(state.map, mapModel -> {
            mapModel.bestWave = Math.max(mapModel.bestWave, state.wave);
            mapModel.save();
        });
    }
}
