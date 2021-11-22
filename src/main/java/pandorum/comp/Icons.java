package pandorum.comp;

import arc.struct.StringMap;
import mindustry.gen.Iconc;

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

                "sharded", Iconc.teamSharded,
                "crux", Iconc.teamCrux,
                "derelict", Iconc.teamDerelict,
                "green", Iconc.statusElectrified,
                "purple", Iconc.statusSporeSlowed,
                "blue", Iconc.statusWet,

                "admin", Iconc.admin
        );
    }

    public static String get(String key) {
        return icons.containsKey(key) ? icons.get(key) : "";
    }
}
