package pandorum.comp;

import arc.struct.StringMap;
import arc.util.Structs;
import mindustry.game.Team;
import mindustry.gen.Iconc;

import static mindustry.Vars.content;
import static pandorum.util.Utils.colorizedTeam;

public class Icons {

    private static StringMap icons;

    public static void init() {
        icons = StringMap.of(
                "copper", Iconc.itemCopper,
                "lead", Iconc.itemLead,
                "metaglass", Iconc.itemMetaglass,
                "graphite", Iconc.itemGraphite,
                "sand", Iconc.itemSand,
                "coal", Iconc.itemCoal,
                "titanium", Iconc.itemTitanium,
                "thorium", Iconc.itemThorium,
                "scrap", Iconc.itemScrap,
                "silicon", Iconc.itemSilicon,
                "plastanium", Iconc.itemPlastanium,
                "phase-fabric", Iconc.itemPhaseFabric,
                "surge-alloy", Iconc.itemSurgeAlloy,
                "spore-pod", Iconc.itemSporePod,
                "blast-compound", Iconc.itemBlastCompound,
                "pyratite", Iconc.itemPyratite,

                "water", Iconc.liquidWater,
                "slag", Iconc.liquidSlag,
                "oil", Iconc.liquidOil,
                "cryofluid", Iconc.liquidCryofluid,

                "dagger", Iconc.unitDagger,
                "mace", Iconc.unitMace,
                "fortress", Iconc.unitFortress,
                "scepter", Iconc.unitScepter,
                "reign", Iconc.unitReign,

                "nova", Iconc.unitNova,
                "pulsar", Iconc.unitPulsar,
                "quasar", Iconc.unitQuasar,
                "vela", Iconc.unitVela,
                "corvus", Iconc.unitCorvus,

                "crawler", Iconc.unitCrawler,
                "atrax", Iconc.unitAtrax,
                "spiroct", Iconc.unitSpiroct,
                "arkyid", Iconc.unitArkyid,
                "toxopid", Iconc.unitToxopid,

                "flare", Iconc.unitFlare,
                "horizon", Iconc.unitHorizon,
                "zenith", Iconc.unitZenith,
                "antumbra", Iconc.unitAntumbra,
                "eclipse", Iconc.unitEclipse,

                "mono", Iconc.unitMono,
                "poly", Iconc.unitPoly,
                "mega", Iconc.unitMega,
                "quad", Iconc.unitQuad,
                "oct", Iconc.unitOct,

                "risso", Iconc.unitRisso,
                "minke", Iconc.unitMinke,
                "bryde", Iconc.unitBryde,
                "sei", Iconc.unitSei,
                "omura", Iconc.unitOmura,

                "retusa", Iconc.unitRetusa,
                "oxynoe", Iconc.unitOxynoe,
                "cyerce", Iconc.unitCyerce,
                "aegires", Iconc.unitAegires,
                "navanax", Iconc.unitNavanax,

                "alpha", Iconc.unitAlpha,
                "beta", Iconc.unitBeta,
                "gamma", Iconc.unitGamma,

                "block", Iconc.unitBlock,

                "spawn", Iconc.blockSpawn,
                "cliff", Iconc.blockCliff,

                "deep-water", Iconc.blockDeepWater,
                "shallow-water", Iconc.blockShallowWater,
                "tainted-water", Iconc.blockTaintedWater,
                "deep-tainted-water", Iconc.blockDeepTaintedWater,
                "darksand-tainted-water", Iconc.blockDarksandTaintedWater,
                "sand-water", Iconc.blockSandWater,
                "darksand-water", Iconc.blockDarksandWater,
                "tar", Iconc.blockTar,
                "pooled-cryofluid", Iconc.blockPooledCryofluid,
                "molten-slag", Iconc.blockMoltenSlag,
                "space", Iconc.blockSpace,

                "stone", Iconc.blockStone,
                "crater-stone", Iconc.blockCraterStone,
                "char", Iconc.blockChar,
                "basalt", Iconc.blockBasalt,
                "hotrock", Iconc.blockHotrock,
                "magmarock", Iconc.blockMagmarock,
                "sand", Iconc.blockSand,
                "darksand", Iconc.blockDarksand,
                "dirt", Iconc.blockDirt,
                "mud", Iconc.blockMud,
                "dacite", Iconc.blockDacite,
                "grass", Iconc.blockGrass,
                "salt", Iconc.blockSalt,
                "snow", Iconc.blockSnow,
                "ice", Iconc.blockIce,
                "ice-snow", Iconc.blockIceSnow,
                "shale", Iconc.blockShale,
                "moss", Iconc.blockMoss,
                "spore-moss", Iconc.blockSporeMoss,

                "stone-wall", Iconc.blockStoneWall,
                "spore-wall", Iconc.blockSporeWall,
                "dirt-wall", Iconc.blockDirtWall,
                "dacite-wall", Iconc.blockDaciteWall,
                "ice-wall", Iconc.blockIceWall,
                "snow-wall", Iconc.blockSnowWall,
                "dune-wall", Iconc.blockDuneWall,
                "sand-wall", Iconc.blockSandWall,
                "salt-wall", Iconc.blockSaltWall,
                "shrubs", Iconc.blockShrubs,
                "shale-wall", Iconc.blockShaleWall,

                "spore-pine", Iconc.blockSporePine,
                "snow-pine", Iconc.blockSnowPine,
                "pine", Iconc.blockPine,
                "white-tree-dead", Iconc.blockWhiteTreeDead,
                "white-tree", Iconc.blockWhiteTree,

                "spore-cluster", Iconc.blockSporeCluster,
                "boulder", Iconc.blockBoulder,
                "snow-boulder", Iconc.blockSnowBoulder,
                "shale-boulder", Iconc.blockShaleBoulder,
                "sand-boulder", Iconc.blockSandBoulder,
                "dacite-boulder", Iconc.blockDaciteBoulder,
                "basalt-boulder", Iconc.blockBasaltBoulder,

                "metal-floor", Iconc.blockMetalFloor,
                "metal-floor-damaged", Iconc.blockMetalFloorDamaged,
                "metal-floor-2", Iconc.blockMetalFloor2,
                "metal-floor-3", Iconc.blockMetalFloor3,
                "metal-floor-4", Iconc.blockMetalFloor4,
                "metal-floor-5", Iconc.blockMetalFloor5,
                "dark-panel-1", Iconc.blockDarkPanel1,
                "dark-panel-2", Iconc.blockDarkPanel2,
                "dark-panel-3", Iconc.blockDarkPanel3,
                "dark-panel-4", Iconc.blockDarkPanel4,
                "dark-panel-5", Iconc.blockDarkPanel5,
                "dark-panel-6", Iconc.blockDarkPanel6,
                "dark-metal", Iconc.blockDarkMetal,

                "pebbles", Iconc.blockPebbles,
                "tendrils", Iconc.blockTendrils,

                "ore-copper", Iconc.blockOreCopper,
                "ore-lead", Iconc.blockOreLead,
                "ore-scrap", Iconc.blockOreScrap,
                "ore-coal", Iconc.blockOreCoal,
                "ore-titanium", Iconc.blockOreTitanium,
                "ore-thorium", Iconc.blockOreThorium,

                "graphite-press", Iconc.blockGraphitePress,
                "multi-press", Iconc.blockMultiPress,
                "silicon-smelter", Iconc.blockSiliconSmelter,
                "silicon-crucible", Iconc.blockSiliconCrucible,
                "kiln", Iconc.blockKiln,
                "plastanium-compressor", Iconc.blockPlastaniumCompressor,
                "phase-weaver", Iconc.blockPhaseWeaver,
                "alloy-smelter", Iconc.blockAlloySmelter,
                "cryofluid-mixer", Iconc.blockCryofluidMixer,
                "pyratite-mixer", Iconc.blockPyratiteMixer,
                "blast-mixer", Iconc.blockBlastMixer,
                "melter", Iconc.blockMelter,
                "separator", Iconc.blockSeparator,
                "disassembler", Iconc.blockDisassembler,
                "spore-press", Iconc.blockSporePress,
                "pulverizer", Iconc.blockPulverizer,
                "coal-centrifuge", Iconc.blockCoalCentrifuge,
                "incinerator", Iconc.blockIncinerator,

                "copper-wall", Iconc.blockCopperWall,
                "copper-wall-large", Iconc.blockCopperWallLarge,
                "titanium-wall", Iconc.blockTitaniumWall,
                "titanium-wall-large", Iconc.blockTitaniumWallLarge,
                "plastanium-wall", Iconc.blockPlastaniumWall,
                "plastanium-wall-large", Iconc.blockPlastaniumWallLarge,
                "thorium-wall", Iconc.blockThoriumWall,
                "thorium-wall-large", Iconc.blockThoriumWallLarge,
                "phase-wall", Iconc.blockPhaseWall,
                "phase-wall-large", Iconc.blockPhaseWallLarge,
                "surge-wall", Iconc.blockSurgeWall,
                "surge-wall-large", Iconc.blockSurgeWallLarge,
                "door", Iconc.blockDoor,
                "door-large", Iconc.blockDoorLarge,
                "scrap-wall", Iconc.blockScrapWall,
                "scrap-wall-large", Iconc.blockScrapWallLarge,
                "scrap-wall-huge", Iconc.blockScrapWallHuge,
                "scrap-wall-gigantic", Iconc.blockScrapWallGigantic,
                "thruster", Iconc.blockThruster,

                "mender", Iconc.blockMender,
                "mend-projector", Iconc.blockMendProjector,
                "overdrive-projector", Iconc.blockOverdriveProjector,
                "overdrive-dome", Iconc.blockOverdriveDome,
                "force-projector", Iconc.blockForceProjector,
                "shock-mine", Iconc.blockShockMine,

                "conveyor", Iconc.blockConveyor,
                "titanium-conveyor", Iconc.blockTitaniumConveyor,
                "plastanium-conveyor", Iconc.blockPlastaniumConveyor,
                "armored-conveyor", Iconc.blockArmoredConveyor,
                "junction", Iconc.blockJunction,
                "bridge-conveyor", Iconc.blockBridgeConveyor,
                "phase-conveyor", Iconc.blockPhaseConveyor,
                "sorter", Iconc.blockSorter,
                "inverted-sorter", Iconc.blockInvertedSorter,
                "router", Iconc.blockRouter,
                "distributor", Iconc.blockDistributor,
                "overflow-gate", Iconc.blockOverflowGate,
                "underflow-gate", Iconc.blockUnderflowGate,
                "mass-driver", Iconc.blockMassDriver,
                "duct", Iconc.blockDuct,
                "duct-router", Iconc.blockDuctRouter,
                "duct-bridge", Iconc.blockDuctRouter,

                "mechanical-pump", Iconc.blockMechanicalPump,
                "rotary-pump", Iconc.blockRotaryPump,
                "thermal-pump", Iconc.blockThermalPump,
                "conduit", Iconc.blockConduit,
                "pulse-conduit", Iconc.blockPulseConduit,
                "plated-conduit", Iconc.blockPlatedConduit,
                "liquid-router", Iconc.blockLiquidRouter,
                "liquid-container", Iconc.blockLiquidContainer,
                "liquid-tank", Iconc.blockLiquidTank,
                "liquid-junction", Iconc.blockLiquidJunction,
                "bridge-conduit", Iconc.blockBridgeConduit,
                "phase-conduit", Iconc.blockPhaseConduit,

                "power-node", Iconc.blockPowerNode,
                "power-node-large", Iconc.blockPowerNodeLarge,
                "surge-tower", Iconc.blockSurgeTower,
                "diode", Iconc.blockDiode,
                "battery", Iconc.blockBattery,
                "battery-large", Iconc.blockBatteryLarge,
                "combustion-generator", Iconc.blockCombustionGenerator,
                "thermal-generator", Iconc.blockThermalGenerator,
                "steam-generator", Iconc.blockSteamGenerator,
                "differential-generator", Iconc.blockDifferentialGenerator,
                "rtg-generator", Iconc.blockRtgGenerator,
                "solar-panel", Iconc.blockSolarPanel,
                "solar-panel-large", Iconc.blockSolarPanelLarge,
                "thorium-reactor", Iconc.blockThoriumReactor,
                "impact-reactor", Iconc.blockImpactReactor,

                "mechanical-drill", Iconc.blockMechanicalDrill,
                "pneumatic-drill", Iconc.blockPneumaticDrill,
                "laser-drill", Iconc.blockLaserDrill,
                "blast-drill", Iconc.blockBlastDrill,
                "water-extractor", Iconc.blockWaterExtractor,
                "cultivator", Iconc.blockCultivator,
                "oil-extractor", Iconc.blockOilExtractor,

                "core-shard", Iconc.blockCoreShard,
                "core-foundation", Iconc.blockCoreFoundation,
                "core-nucleus", Iconc.blockCoreNucleus,
                "vault", Iconc.blockVault,
                "container", Iconc.blockContainer,
                "unloader", Iconc.blockUnloader,

                "duo", Iconc.blockDuo,
                "scatter", Iconc.blockScatter,
                "scorch", Iconc.blockScorch,
                "hail", Iconc.blockHail,
                "wave", Iconc.blockWave,
                "lancer", Iconc.blockLancer,
                "arc", Iconc.blockArc,
                "parallax", Iconc.blockParallax,
                "swarmer", Iconc.blockSwarmer,
                "salvo", Iconc.blockSalvo,
                "segment", Iconc.blockSegment,
                "tsunami", Iconc.blockTsunami,
                "fuse", Iconc.blockFuse,
                "ripple", Iconc.blockRipple,
                "cyclone", Iconc.blockCyclone,
                "foreshadow", Iconc.blockForeshadow,
                "spectre", Iconc.blockSpectre,
                "meltdown", Iconc.blockMeltdown,

                "command-center", Iconc.blockCommandCenter,
                "ground-factory", Iconc.blockGroundFactory,
                "air-factory", Iconc.blockAirFactory,
                "naval-factory", Iconc.blockNavalFactory,
                "additive-reconstructor", Iconc.blockAdditiveReconstructor,
                "multiplicative-reconstructor", Iconc.blockMultiplicativeReconstructor,
                "exponential-reconstructor", Iconc.blockExponentialReconstructor,
                "tetrative-reconstructor", Iconc.blockTetrativeReconstructor,
                "repair-point", Iconc.blockRepairPoint,
                "repair-turret", Iconc.blockRepairTurret,

                "payload-conveyor", Iconc.blockPayloadConveyor,
                "payload-router", Iconc.blockPayloadRouter,
                "payload-propulsion-tower", Iconc.blockPayloadPropulsionTower,
                "deconstructor", Iconc.blockDeconstructor,
                "constructor", Iconc.blockConstructor,
                "large-constructor", Iconc.blockLargeConstructor,
                "payload-loader", Iconc.blockPayloadLoader,
                "payload-unloader", Iconc.blockPayloadUnloader,

                "power-source", Iconc.blockPowerSource,
                "power-void", Iconc.blockPowerVoid,
                "item-source", Iconc.blockItemSource,
                "item-void", Iconc.blockItemVoid,
                "liquid-source", Iconc.blockLiquidSource,
                "liquid-void", Iconc.blockLiquidVoid,
                "payload-source", Iconc.blockPayloadSource,
                "payload-void", Iconc.blockPayloadVoid,
                "illuminator", Iconc.blockIlluminator,

                "launch-pad", Iconc.blockLaunchPad,
                "interplanetary-accelerator", Iconc.blockInterplanetaryAccelerator,

                "message", Iconc.blockMessage,
                "switch", Iconc.blockSwitch,
                "micro-processor", Iconc.blockMicroProcessor,
                "logic-processor", Iconc.blockLogicProcessor,
                "hyper-processor", Iconc.blockHyperProcessor,
                "memory-cell", Iconc.blockMemoryCell,
                "memory-bank", Iconc.blockMemoryBank,
                "logic-display", Iconc.blockLogicDisplay,
                "large-logic-display", Iconc.blockLargeLogicDisplay,

                "sharded", Iconc.teamSharded,
                "crux", Iconc.teamCrux,
                "derelict", Iconc.teamDerelict,
                "green", Iconc.statusElectrified,
                "purple", Iconc.statusSporeSlowed,
                "blue", Iconc.statusWet,

                "attack", Iconc.commandAttack,
                "rally", Iconc.commandRally,
                "idle", Iconc.cancel
        );
    }

    public static String get(String key) {
        return get(key, "");
    }

    public static String get(String key, String defaultValue) {
        return icons.get(key, defaultValue);
    }

    public static String unitsList() {
        StringBuilder units = new StringBuilder();
        content.units().each(unit -> units.append(" ").append(get(unit.name)).append(unit.name));
        return units.toString();
    }

    public static String itemsList() {
        StringBuilder items = new StringBuilder();
        content.items().each(item -> items.append(" ").append(get(item.name)).append(item.name));
        return items.toString();
    }

    public static String teamsList() {
        StringBuilder teams = new StringBuilder();
        Structs.each(team -> teams.append("\n[gold] - [white]").append(colorizedTeam(team)), Team.baseTeams);
        return teams.toString();
    }
}
