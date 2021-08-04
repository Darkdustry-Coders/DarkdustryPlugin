package pandorum.entry;

import arc.util.*;
import arc.struct.*;
import mindustry.content.Blocks;
import mindustry.entities.units.UnitCommand;
import mindustry.game.EventType.ConfigEvent;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.units.*;
import static pandorum.Misc.*;

import java.util.concurrent.TimeUnit;
import java.util.*;

import static mindustry.Vars.world;
import pandorum.comp.*;

public class ConfigEntry implements HistoryEntry{
    private static final StringMap icons = StringMap.of(
            "copper", "\uF838",
            "lead", "\uF837",
            "metaglass", "\uF836",
            "graphite", "\uF835",
            "sand", "\uF834",
            "coal", "\uF833",
            "titanium", "\uF832",
            "thorium", "\uF831",
            "scrap", "\uF830",
            "silicon", "\uF82F",
            "plastanium", "\uF82E",
            "phase-fabric", "\uF82D",
            "surge-alloy", "\uF82C",
            "spore-pod", "\uF82B",
            "blast-compound", "\uF82A",
            "pyratite", "\uF829",

            "water", "\uF828",
            "slag", "\uF827",
            "oil", "\uF826",
            "cryofluid", "\uF825",

            "white-tree-dead", "\uf8db",
            "white-tree", "\uf8da",
            "spore-cluster", "\uf8d9",
            "boulder", "\uf7cb",
            "snow-boulder", "\uf7ca",
            "shale-boulder", "\uf8d6",
            "sand-boulder", "\uf8d5",
            "dacite-boulder", "\uf7c8",
            "basalt-boulder", "\uf7b8",
            "graphite-press", "\uf8be",
            "multi-press", "\uf8bd",
            "silicon-smelter", "\uf8bc",
            "silicon-crucible", "\uf80f",
            "kiln", "\uf8bb",
            "plastanium-compressor", "\uf8ba",
            "phase-weaver", "\uf8b9",
            "alloy-smelter", "\uf8b8",
            "cryofluid-mixer", "\uf8b7",
            "pyratite-mixer", "\uf8b5",
            "blast-mixer", "\uf8b6",
            "melter", "\uf8b4",
            "separator", "\uf8b3",
            "disassembler", "\uf80c",
            "spore-press", "\uf8b2",
            "pulverizer", "\uf8b1",
            "coal-centrifuge", "\uf8b0",
            "incinerator", "\uf8af",
            "copper-wall", "\uf8ae",
            "copper-wall-large", "\uf8ad",
            "titanium-wall", "\uf8ac",
            "titanium-wall-large", "\uf8ab",
            "plastanium-wall", "\uf8aa",
            "plastanium-wall-large", "\uf8a9",
            "thorium-wall", "\uf8a8",
            "thorium-wall-large", "\uf8a7",
            "phase-wall", "\uf8a6",
            "phase-wall-large", "\uf8a5",
            "surge-wall", "\uf8a4",
            "surge-wall-large", "\uf8a3",
            "door", "\uf8a2",
            "door-large", "\uf8a1",
            "scrap-wall", "\uf8a0",
            "scrap-wall-large", "\uf89f",
            "scrap-wall-huge", "\uf89e",
            "scrap-wall-gigantic", "\uf89d",
            "thruster", "\uf89c",
            "mender", "\uf89b",
            "mend-projector", "\uf89a",
            "overdrive-projector", "\uf899",
            "overdrive-dome", "\uf7e6",
            "force-projector", "\uf898",
            "shock-mine", "\uf897",
            "conveyor", "\uf896",
            "titanium-conveyor", "\uf895",
            "plastanium-conveyor", "\uf819",
            "armored-conveyor", "\uf894",
            "junction", "\uf893",
            "bridge-conveyor", "\uf892",
            "phase-conveyor", "\uf891",
            "sorter", "\uf890",
            "inverted-sorter", "\uf88f",
            "router", "\uf88e",
            "distributor", "\uf88d",
            "overflow-gate", "\uf88c",
            "underflow-gate", "\uf824",
            "mass-driver", "\uf88b",
            "payload-conveyor", "\uf7e0",
            "payload-router", "\uf810",
            "mechanical-pump", "\uf88a",
            "rotary-pump", "\uf889",
            "thermal-pump", "\uf888",
            "conduit", "\uf887",
            "pulse-conduit", "\uf886",
            "plated-conduit", "\uf885",
            "liquid-router", "\uf884",
            "liquid-tank", "\uf883",
            "liquid-junction", "\uf882",
            "bridge-conduit", "\uf881",
            "phase-conduit", "\uf880",
            "power-node", "\uf87f",
            "power-node-large", "\uf87e",
            "surge-tower", "\uf87d",
            "diode", "\uf87c",
            "battery", "\uf87b",
            "battery-large", "\uf87a",
            "combustion-generator", "\uf879",
            "thermal-generator", "\uf878",
            "steam-generator", "\uf877",
            "differential-generator", "\uf876",
            "rtg-generator", "\uf875",
            "solar-panel", "\uf874",
            "solar-panel-large", "\uf873",
            "thorium-reactor", "\uf872",
            "impact-reactor", "\uf871",
            "mechanical-drill", "\uf870",
            "pneumatic-drill", "\uf86f",
            "laser-drill", "\uf86e",
            "blast-drill", "\uf86d",
            "water-extractor", "\uf86c",
            "cultivator", "\uf86b",
            "oil-extractor", "\uf86a",
            "core-shard", "\uf869",
            "core-foundation", "\uf868",
            "core-nucleus", "\uf867",
            "vault", "\uf866",
            "container", "\uf865",
            "unloader", "\uf864",
            "duo", "\uf861",
            "scatter", "\uf860",
            "scorch", "\uf85f",
            "hail", "\uf85e",
            "wave", "\uf85d",
            "lancer", "\uf85c",
            "arc", "\uf85b",
            "parallax", "\uf801",
            "swarmer", "\uf85a",
            "salvo", "\uf859",
            "segment", "\uf80e",
            "tsunami", "\uf7bd",
            "fuse", "\uf858",
            "ripple", "\uf857",
            "cyclone", "\uf856",
            "foreshadow", "\uf7be",
            "spectre", "\uf855",
            "meltdown", "\uf854",
            "command-center", "\uf850",
            "ground-factory", "\uf81f",
            "air-factory", "\uf816",
            "naval-factory", "\uf817",
            "additive-reconstructor", "\uf806",
            "multiplicative-reconstructor", "\uf805",
            "exponential-reconstructor", "\uf804",
            "tetrative-reconstructor", "\uf803",
            "repair-point", "\uf848",
            "resupply-point", "\uf802",
            "power-source", "\uf840",
            "power-void", "\uf83f",
            "item-source", "\uf83e",
            "item-void", "\uf83d",
            "liquid-source", "\uf83c",
            "liquid-void", "\uf83b",
            "illuminator", "\uf839",
            "launch-pad", "\uf863",
            "launch-pad-large", "\uf862",
            "interplanetary-accelerator", "\uf7b9",
            "message", "\uf83a",
            "switch", "\uf7e2",
            "micro-processor", "\uf7e4",
            "logic-processor", "\uf7e5",
            "hyper-processor", "\uf7df",
            "memory-cell", "\uf7e1",
            "memory-bank", "\uf7bf",
            "logic-display", "\uf7e3",
            "large-logic-display", "\uf7c7",
            "block-forge", "\uf81b",
            "block-loader", "\uf814",
            "block-unloader", "\uf813",

            "dagger", "",
            "mace", "",
            "fortress", "",
            "scepter", "",
            "reign", "",

            "nova", "",
            "pulsar", "",
            "quasar", "",
            "vela", "",
            "corvus", "",

            "crawler", "",
            "atrax", "",
            "spiroct", "",
            "arkyid", "",
            "toxopid", "",

            "flare", "",
            "horizon", "",
            "zenith", "",
            "antumbra", "",
            "eclipse", "",

            "mono", "",
            "poly", "",
            "mega", "",
            "quad", "",
            "oct", "",

            "risso", "",
            "minke", "",
            "bryde", "",
            "sei", "",
            "omura", "",

            "retusa", "",
            "oxynoe", "",
            "cyerce", "",
            "aegires", "",
            "navanax", "",

            "alpha", "",
            "beta", "",
            "gamma", ""
    );

    private static final String[] commands;

    static{
        commands = bundle.get("events.history.config.command-center.all").split(", ");
    }

    public final long lastAccessTime = Time.millis();
    public final String name;
    public final Block block;
    public final Object value;
    public final boolean connect;
    public final Building build;

    public ConfigEntry(ConfigEvent event, boolean connect){
        this.name = Groups.player.contains(p -> event.player == p) ? colorizedName(event.player) : bundle.get("events.unknown");
        this.block = event.tile.block();
        this.value = event.value;
        this.connect = connect;
        this.build = event.tile;
    }

    @Override
    public String getMessage(Player player){
        if(block == Blocks.powerNode || block == Blocks.powerNodeLarge || block == Blocks.powerSource ||
           block == Blocks.powerVoid || block == Blocks.surgeTower || block == Blocks.phaseConduit || block == Blocks.phaseConveyor ||
           block == Blocks.bridgeConduit || block == Blocks.itemBridge || block == Blocks.massDriver || block == Blocks.ductBridge || block == Blocks.payloadPropulsionTower){
            int data = (int)value;
            Tile tile = world.tile(data);
            if(tile == null){
                return bundle.get("events.history.unknown", findLocale(player.locale));
            }

            if(connect){
                return bundle.format("events.history.config.power-node.connect", findLocale(player.locale), name, block, tile.x, tile.y);
            }

            return bundle.format("events.history.config.power-node.disconnect", findLocale(player.locale), name, block, tile.x, tile.y);
        }

        if(block == Blocks.door || block == Blocks.doorLarge){
            boolean data = (boolean)value;
            return data ? bundle.format("events.history.config.door.on", findLocale(player.locale), name, block) : bundle.format("events.history.config.door.off", findLocale(player.locale), name, block);
        }

        if(block == Blocks.switchBlock){
            boolean data = (boolean)value;
            return data ? bundle.format("events.history.config.switch.on", findLocale(player.locale), name) : bundle.format("events.history.config.switch.off", findLocale(player.locale), name);
        }

        if(block == Blocks.commandCenter){
            return bundle.format("events.history.config.command-center", findLocale(player.locale), name, commands[((UnitCommand)value).ordinal()]);
        }

        if(block == Blocks.liquidSource){
            Liquid liquid = (Liquid)value;
            if(liquid == null){
                return bundle.format("events.history.config.default", findLocale(player.locale), name);
            }

            return bundle.format("events.history.config", findLocale(player.locale), name, icons.get(liquid.name));
        }

        if(block == Blocks.unloader || block == Blocks.sorter || block == Blocks.invertedSorter || block == Blocks.itemSource){
            Item item = (Item)value;
            if(item == null){
                return bundle.format("events.history.config.default", findLocale(player.locale), name);
            }

            return bundle.format("events.history.config", findLocale(player.locale), name, icons.get(item.name));
        }
        if(block == Blocks.navalFactory || block == Blocks.airFactory || block == Blocks.groundFactory || block == Blocks.blockForge){
            return bundle.format("events.history.config.changed", findLocale(player.locale), name);
        }
        return bundle.get("events.history.unknown", findLocale(player.locale)); // ага да
    }

    @Override
    public long getLastAccessTime(TimeUnit unit){
        return unit.convert(Time.timeSinceMillis(lastAccessTime), TimeUnit.MILLISECONDS);
    }
}
