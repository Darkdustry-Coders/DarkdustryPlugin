package darkdustry.features;

import arc.func.Func;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.ai.UnitCommand;
import mindustry.content.Blocks;
import mindustry.entities.units.AIController;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.type.Item;
import mindustry.world.Tile;

import static darkdustry.config.Config.config;
import static mindustry.Vars.indexer;

public class PolymerAI extends AIController {
    // We love AI. Especially the borked ones.
    public static void load() {
        if (config.overrideMonoAi)
            try {
                var field = UnitCommand.mineCommand.getClass().getField("controller");
                field.setAccessible(true);
                field.set(UnitCommand.mineCommand, (Func<Unit, AIController>) unit -> new PolymerAI());
                Log.info("Using PolymerAI");
            } catch (Exception e) {
                Log.warn("Failed to apply PolymerAI: ", e);
            }
    }

    public boolean setup = false;
    public @Nullable Item targetItem = null;
    public @Nullable Tile ore = null;

    void debug(String message) {
        Groups.player.each(x -> x.sendMessage(message));
    }

    @Override
    public void updateMovement() {
        if (!setup) {
            setup = true;

            debug("e");
        }
        if (unit.stack.amount >= unit.type.itemCapacity) {
            var core = unit.closestCore();
            unit.mineTile = null;

            if (unit.within(core, unit.type.range)) {
                if (core.acceptStack(unit.stack.item, unit.stack.amount, unit) > 0) {
                    Call.transferItemTo(unit, unit.stack.item, unit.stack.amount, unit.x, unit.y, core);
                }
                unit.clearItem();
            }
            else {
                circle(core, unit.type.range / 1.8f);
                return;
            }
        }
        if (ore != null && !unit.validMine(ore)) {
            ore = null;
            unit.mineTile = null;
        }
        if (ore == null) {
            var core = unit.closestCore();
            targetItem = unit.type.mineItems.min(i -> indexer.hasOre(i) && unit.canMine(i), i -> core.items.get(i));

            if (targetItem == null || core.acceptStack(targetItem, 1, unit) == 0){
                unit.clearItem();
                unit.mineTile = null;
                return;
            }

            ore = indexer.findClosestOre(unit, targetItem);
            unit.mineTile(ore);

            if (ore != null)
                debug("selected new ore at: " + ore.x + "x" + ore.y);
            else
                debug("selected no ore");
        }
        if (ore != null) {
            if (ore.block() != Blocks.air) {
                unit.clearItem();
                unit.mineTile = null;
                return;
            }
        }
        if (unit.mineTile != null) {
            if (unit.within(ore, unit.type.mineRange)) {
                moveTo(ore, unit.type.mineRange);
            }
        }


        //if (!(unit.canMine()) || core == null) return;

        //if (unit.mineTile != null && !unit.mineTile.within(unit, unit.type.mineRange)) {
        //    unit.mineTile(null);
        //}

        //if (ore != null && !unit.validMine(ore)) {
        //    ore = null;
        //    unit.mineTile = null;
        //}

        //if (mining) {
        //    if (timer.get(timerTarget2, 60 * 4) || targetItem == null) {
        //        targetItem = unit.type.mineItems.min(i -> indexer.hasOre(i) && unit.canMine(i), i -> core.items.get(i));
        //    }

        //    // core full of the target item, do nothing
        //    if (targetItem != null && core.acceptStack(targetItem, 1, unit) == 0){
        //        unit.clearItem();
        //        unit.mineTile = null;
        //        return;
        //    }

        //    // if inventory is full, drop it off.
        //    if (unit.stack.amount >= unit.type.itemCapacity || (targetItem != null && !unit.acceptsItem(targetItem))) {
        //        mining = false;
        //    } else {
        //        if (timer.get(timerTarget3, 60) && targetItem != null) {
        //            ore = indexer.findClosestOre(unit, targetItem);
        //        }

        //        if (ore != null){
        //            moveTo(ore, unit.type.mineRange / 2f, 20f);

        //            if (ore.block() == Blocks.air && unit.within(ore, unit.type.mineRange)){
        //                unit.mineTile = ore;
        //            }

        //            if (ore.block() != Blocks.air){
        //                mining = false;
        //            }
        //        }
        //    }
        //} else {
        //    unit.mineTile = null;

        //    if (unit.stack.amount == 0) {
        //        mining = true;
        //        return;
        //    }

        //    if (unit.within(core, unit.type.range)) {
        //        if (core.acceptStack(unit.stack.item, unit.stack.amount, unit) > 0) {
        //            Call.transferItemTo(unit, unit.stack.item, unit.stack.amount, unit.x, unit.y, core);
        //        }

        //        unit.clearItem();
        //        mining = true;
        //    }

        //    circle(core, unit.type.range / 1.8f);
        //}
    }
}
