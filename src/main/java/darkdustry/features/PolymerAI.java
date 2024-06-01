package darkdustry.features;

import arc.func.Func;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Timer;
import mindustry.ai.UnitCommand;
import mindustry.ai.types.MinerAI;
import mindustry.content.Blocks;
import mindustry.entities.units.AIController;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.world.Tile;

import static darkdustry.config.Config.config;
import static mindustry.Vars.indexer;

public class PolymerAI extends AIController {
    // We love AI. Especially the borked ones.
    public static void load() {
        if (config.overrideMonoAi) {
            try {
                var field = UnitCommand.mineCommand.getClass().getField("controller");
                field.setAccessible(true);
                field.set(UnitCommand.mineCommand, (Func<Unit, AIController>) unit -> new PolymerAI());
                if (!((UnitCommand.mineCommand.controller.get(null)) instanceof PolymerAI)) {
                    Log.warn("Failed to apply PolymerAI: Silent failure");
                } else
                    Log.info("Using PolymerAI");
            } catch (Exception e) {
                Log.warn("Failed to apply PolymerAI: ", e);
            }

            // So dirty, but if it works, it works
            Timer.schedule(() -> {
                Groups.unit.each(x -> x.controller() instanceof MinerAI, x -> {
                    x.controller(new PolymerAI());
                });
            }, 0.1f, 0.1f);
        }
    }

    public @Nullable Tile targetTile = null;
    public int trackerVar = 0;

    @Override
    public void updateMovement() {
        final var core = unit.core();
        if (!(unit.canMine()) || core == null) return;
        if (targetTile != null && targetTile.block() != Blocks.air) {
            targetTile = null;
            unit.mineTile = null;
        }
        if (targetTile == null || unit.stack.amount < trackerVar && unit.stack.item != null) {
            targetTile = null;
            unit.mineTile = null;

            var item = unit.type.mineItems.min(i -> indexer.hasOre(i) && unit.canMine(i), i -> core.items.get(i));
            if (item != unit.stack.item) unit.clearItem();
            if (item == null) return;
            if (core.acceptStack(item, 1, unit) == 0) return;
            var tile = indexer.findClosestOre(unit, item);
            if (tile == null) return;
            targetTile = tile;
        }
        trackerVar = unit.stack.amount;
        if (unit.stack.amount >= unit.type.itemCapacity) {
            targetTile = null;
            unit.mineTile = null;

            var core$ = unit.closestCore();

            if (!unit.within(core$, unit.type.range)) {
                circle(core$, unit.type.range);
            }
            else {
                Call.transferItemTo(unit, unit.stack.item, unit.stack.amount, unit.x, unit.y, core$);
                unit.clearItem();
            }

            return;
        }
        if (!unit.within(targetTile, unit.type.mineRange / 2)) {
            moveTo(targetTile, unit.type.mineRange / 2, 20f);
        }
        if (unit.within(targetTile, unit.type.mineRange))
            unit.mineTile(targetTile);
        else {
            unit.mineTile(null);
        }
    }
}
