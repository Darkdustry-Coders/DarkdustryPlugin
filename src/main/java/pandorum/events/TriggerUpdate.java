package pandorum.events;

import mindustry.gen.Player;
import mindustry.gen.Groups;

import static pandorum.effects.Effects.onJoin;
import pandorum.PandorumPlugin;
import pandorum.comp.Config.PluginType;
import static pandorum.effects.Effects.onMove;

public class TriggerUpdate {
    public static void call() {
        Groups.player.each(p -> p.unit().moving(), p -> onMove(p));
        if(PandorumPlugin.config.type == PluginType.sand || PandorumPlugin.config.type == PluginType.anarchy) {          
            final float despawnDelay = Core.settings.getFloat("despawndelay", this.defDelay);
            Groups.unit.each(unit -> {
                if (Time.globalTime - (float)this.timer.get(unit, () -> Time.globalTime) >= despawnDelay) {
                    unit.spawnedByCore(true);
                }
            });
            for (final Unit key : this.timer.keys()) {
                if (key == null) return;
                if (key.isValid()) continue;
                this.timer.remove(key);
            }
        }
    }
}
