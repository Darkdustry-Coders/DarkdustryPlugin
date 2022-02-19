package pandorum.events.listeners;

import pandorum.database.databridges.MapInfo;

import static mindustry.Vars.state;

public class WaveListener {

    public static void call() {
        MapInfo.find(state.map, mapModel -> {
            mapModel.bestWave = Math.max(mapModel.bestWave, state.wave);
            MapInfo.save(mapModel);
        });
    }
}
