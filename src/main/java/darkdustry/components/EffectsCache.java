package darkdustry.components;

import arc.struct.IntMap;
import darkdustry.features.menus.MenuHandler.EffectsPack;
import mindustry.gen.Player;

public class EffectsCache {

    public static final IntMap<EffectsPack> cache = new IntMap<>();

    public static void updateEffects(Player player, EffectsPack effects) {
        cache.put(player.id, effects);
    }

    public static void join(Player player) {
        var effects = cache.get(player.id);
        if (effects == null) return;

        effects.join.get(player);
    }

    public static void leave(Player player) {
        var effects = cache.remove(player.id);
        if (effects == null) return;

        effects.leave.get(player);
    }

    public static void move(Player player) {
        if (!player.unit().moving()) return; // It's called here to prevent NPE

        var effects = cache.get(player.id);
        if (effects == null) return;

        effects.move.get(player);
    }
}