package darkdustry.features;

import arc.Events;
import arc.func.Func;
import arc.struct.IntSeq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.ai.UnitCommand;
import mindustry.ai.types.MinerAI;
import mindustry.content.Blocks;
import mindustry.entities.units.AIController;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.type.Item;
import mindustry.world.Tile;

import java.util.Arrays;

import static darkdustry.config.Config.config;

public class PolymerAI extends AIController {
    private static IntSeq[] oreLocations = new IntSeq[Vars.content.items().size];
    private static int[] oreCount = new int[Vars.content.items().size];
    private static boolean skip = true;

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
            Timer.schedule(() -> Groups.unit.each(x -> x.controller() instanceof MinerAI, x -> {
                x.controller(new PolymerAI());
            }), 0.1f, 0.1f);

            Events.on(EventType.WorldLoadBeginEvent.class, _unused -> {
                skip = true;
            });

            Events.on(EventType.WorldLoadEndEvent.class, _unused -> {
                for (var arr : oreLocations) {
                    if (arr != null) arr.clear();
                }
                Arrays.fill(oreCount, 0);

                Vars.world.tiles.eachTile(x -> {
                    if (validTile(x)) {
                        var id = x.drop().id;

                        if (oreLocations[id] == null)
                            oreLocations[id] = new IntSeq(32);

                        oreLocations[id].add(x.pos());
                        oreCount[id]++;
                    }
                });

                skip = false;
            });

            Events.on(EventType.TilePreChangeEvent.class, e -> removeTile(e.tile));
            Events.on(EventType.TileChangeEvent.class, e -> addTile(e.tile));
        }
    }

    public @Nullable Tile targetTile = null;
    public int prevItemCount = 0;

    private static void addTile(Tile x) {
        if (skip) return;
        if (validTile(x)) {
            var id = x.drop().id;

            if (oreLocations[id] == null)
                oreLocations[id] = new IntSeq(32);
            else if (oreLocations[id].contains(x.pos())) return;

            int index = oreLocations[id].indexOf(-1);
            if (index != -1) {
                oreLocations[id].set(index, x.pos());
            }
            else oreLocations[id].add(x.pos());

            oreCount[id]++;
        }
    }

    private static void removeTile(Tile x) {
        if (skip) return;
        if (!validTile(x) && x.drop() != null) {
            removeTile(x, x.drop().id);
        }
    }
    private static void removeTile(Tile x, int itemid) {
        if (oreLocations[itemid] == null) return;

        int index = oreLocations[itemid].indexOf(x.pos());
        if (index == -1) return;
        oreLocations[itemid].set(index, -1);

        oreCount[itemid]--;
    }

    private static boolean validTile(@Nullable Tile x) {
        return x != null && x.drop() != null && x.block() == Blocks.air;
    }

    private static boolean hasOre(Item ore) {
        return oreCount[ore.id] > 0;
    }

    private static @Nullable Tile findClosestOre(Unit unit, Item item) {
        if (oreLocations[item.id] == null) return null;
        if (oreCount[item.id] <= 0) return null;

        @Nullable Tile target = null;
        float distance = Float.POSITIVE_INFINITY;

        for (int i = 0; i < oreLocations[item.id].size; i++) {
            var pos = oreLocations[item.id].get(i);
            if (pos == -1) continue;

            var tile = Vars.world.tile(pos);
            if (!validTile(tile)) {
                oreLocations[item.id].set(i, -1);
                oreCount[item.id]--;
                continue;
            }

            float dist2 = unit.dst(tile);
            if (dist2 < distance) {
                distance = dist2;
                target = tile;
            }
        }

        return target;
    }

    @Override
    public void updateMovement() {
        final var core = unit.core();
        if (!(unit.canMine()) || core == null) return;
        if (targetTile != null && targetTile.block() != Blocks.air) {
            targetTile = null;
            unit.mineTile = null;
        }
        if (!validTile(targetTile) || unit.stack.amount < prevItemCount && unit.stack.item != null) {
            targetTile = null;
            unit.mineTile = null;

            var item = unit.type.mineItems.min(i -> hasOre(i) && unit.canMine(i), i -> core.items.get(i));
            if (item != unit.stack.item) unit.clearItem();
            if (item == null) return;
            if (core.acceptStack(item, 1, unit) == 0) return;
            var tile = findClosestOre(unit, item);
            if (tile == null) return;
            targetTile = tile;
        }
        prevItemCount = unit.stack.amount;
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
