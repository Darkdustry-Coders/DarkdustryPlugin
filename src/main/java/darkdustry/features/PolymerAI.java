package darkdustry.features;

import arc.Events;
import arc.util.Nullable;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.ai.UnitCommand;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class PolymerAI {
    private enum PolymerState {
        doingNothing,
        mining,
        flyingBack,
    }

    private static Map<Unit, PolymerState> units = new HashMap<>();

    // We love AI. Especially the borked ones.
    public static void init() {
        Events.on(EventType.UnitDestroyEvent.class, e -> {
            units.remove(e.unit);
        });

        Timer.schedule(() -> Groups.unit.each(PolymerAI::update), 0.1f, 0.1f);
    }

    private static void update(Unit unit) {
        if (unit.type() == UnitTypes.mono) makeEmMine(unit);
        if (unit.type() == UnitTypes.poly && unit.command().command == UnitCommand.mineCommand) makeEmMine(unit);
    }

    private static void makeEmMine(Unit unit) {
        PolymerState state = units.computeIfAbsent(unit, k -> PolymerState.doingNothing);

        switch (state) {
            case doingNothing -> {
                int tier = unit.type().mineTier;

                Building core = unit.team().core();

                Floor seek = Blocks.oreCopper.asFloor();
                int count = core.items().get(Items.copper);

                {
                    int oc = core.items().get(Items.lead);
                    if (count > oc) {
                        count = oc;
                        seek = Blocks.oreLead.asFloor();
                    }
                }

                if (tier > 2) {
                    int oc = core.items().get(Items.titanium);
                    if (count > oc) {
                        count = oc;
                        seek = Blocks.oreTitanium.asFloor();
                    }
                }

                Floor finalSeek = seek;
                AtomicReference<Tile> aTile = new AtomicReference<>(null);
                AtomicReference<Double> distance = new AtomicReference<>(Double.POSITIVE_INFINITY);

                Vars.world.tiles.eachTile(t -> {
                    double dist = Math.sqrt((t.x - unit.x) * (t.x - unit.x) + (t.y - unit.y) * (t.y - unit.y));
                    if (distance.get() < dist) return;
                    if (t.overlay() != finalSeek) return;
                    if (t.block() != Blocks.air) return;

                    distance.set(dist);

                });

                @Nullable var tile = aTile.get();

                if (tile == null) return;

                unit.mineTile(tile);
                units.put(unit, PolymerState.mining);
            }
            case mining -> {}
            case flyingBack -> {}
        }

    }
}
