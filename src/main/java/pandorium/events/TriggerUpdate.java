package pandorium.events;

import arc.Core;
import arc.util.Time;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import pandorium.PandorumPlugin;
import pandorium.comp.Config.PluginType;
import pandorium.effects.Effects;

public class TriggerUpdate {
    public static void call() {
        Groups.player.each(p -> p.unit().moving(), Effects::onMove);
        if(PandorumPlugin.config.type == PluginType.sand || PandorumPlugin.config.type == PluginType.anarchy) {          
            final float despawnDelay = Core.settings.getFloat("despawndelay", 36000f);
            Groups.unit.each(unit -> {
                if (Time.globalTime - PandorumPlugin.timer.get(unit, () -> Time.globalTime) >= despawnDelay) unit.spawnedByCore(true);
            });
            for (final Unit key : PandorumPlugin.timer.keys()) {
                if (key == null) return;
                if (key.isValid()) continue;
                PandorumPlugin.timer.remove(key);
            }
        }
    }
}
