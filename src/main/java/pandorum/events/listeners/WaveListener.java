package pandorum.events.listeners;

import static mindustry.Vars.state;
import static pandorum.PluginVars.mapsInfo;

public class WaveListener {

    public static void call() {
        mapsInfo.find(state.map, mapModel -> {
            mapModel.bestWave = Math.max(mapModel.bestWave, state.wave);
            mapModel.save();
        });
    }
}
