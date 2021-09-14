package pandorum.entry;

import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Pack;
import mindustry.content.Blocks;
import mindustry.entities.units.UnitCommand;
import mindustry.game.EventType.ConfigEvent;
import mindustry.gen.Building;
import mindustry.gen.Player;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.Door;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.blocks.units.UnitFactory;
import pandorum.comp.Bundle;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

import static mindustry.Vars.world;
import static pandorum.Misc.colorizedName;
import static pandorum.Misc.findLocale;

public class ConfigEntry implements HistoryEntry{
    public String name;
    public Block block;
    public Object value;
    public boolean connect;
    public Building build;
    public Date time;

    public ConfigEntry(ConfigEvent event, boolean connect){
        this.name = colorizedName(event.player);
        this.block = event.tile.block();
        this.build = event.tile;
        this.value = build instanceof UnitFactory.UnitFactoryBuild ? ((UnitFactory.UnitFactoryBuild)build).unit() : getConfig(event);
        this.connect = connect;
        this.time = new Date();
    }

    private Object getConfig(ConfigEvent event) {
        if (block.configurations.containsKey(Integer.class) &&
                (block.configurations.containsKey(Point2[].class) || block.configurations.containsKey(Point2.class))) {
            int count;
            if (block instanceof PowerNode) {
                count = build != null ? build.getPowerConnections(new Seq<>()).size : 0;
            } else {
                count = build != null ? (int) event.value : -1;
            }

            return Pack.longInt(count, (int) event.value);
        }
        return event.value;
    }

    @Override
    public String getMessage(Player player){
        final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Moscow")));
        final String ftime = df.format(this.time);

        if (block.configurations.containsKey(Integer.class) &&
                (block.configurations.containsKey(Point2[].class) || block.configurations.containsKey(Point2.class))) {
            int data = Pack.rightInt((long) value);
            if (data < 0) {
                return Bundle.format("history.config.disconnect", findLocale(player.locale), name, block, ftime);
            }

            Tile tile = world.tile(data);
            if (tile == null) {
                return Bundle.format("history.config.changed", findLocale(player.locale));
            }

            if (connect) {
                return Bundle.format("history.config.connect", findLocale(player.locale), name, block, tile.x, tile.y, ftime);
            }

            return Bundle.format("history.config.power-node.disconnect", findLocale(player.locale), name, block, tile.x, tile.y, ftime);
        }

        if(block instanceof Door){
            boolean data = (boolean)value;
            return data ? Bundle.format("history.config.door.on", findLocale(player.locale), name, block, ftime) : Bundle.format("history.config.door.off", findLocale(player.locale), name, block, ftime);
        }

        if(block == Blocks.switchBlock){
            boolean data = (boolean)value;
            return data ? Bundle.format("history.config.switch.on", findLocale(player.locale), name, ftime) : Bundle.format("history.config.switch.off", findLocale(player.locale), name, ftime);
        }

        if(block == Blocks.commandCenter){
            final String[] commands = Bundle.get("history.config.command-center.all", findLocale(player.locale)).split(", ");
            return Bundle.format("history.config.command-center", findLocale(player.locale), name, commands[((UnitCommand)value).ordinal()], ftime);
        }

        if(block == Blocks.liquidSource){
            Liquid liquid = (Liquid)value;
            if(liquid == null){
                return Bundle.format("history.config.default", findLocale(player.locale), name, ftime);
            }

            return Bundle.format("history.config", findLocale(player.locale), name, icons.get(liquid.name), ftime);
        }

        if(block == Blocks.unloader || block == Blocks.sorter || block == Blocks.invertedSorter || block == Blocks.itemSource){
            Item item = (Item)value;
            if(item == null){
                return Bundle.format("history.config.default", findLocale(player.locale), name, ftime);
            }

            return Bundle.format("history.config", findLocale(player.locale), name, icons.get(item.name), ftime);
        }

        if(block instanceof UnitFactory){
            UnitType unit = (UnitType)value;
            if(unit == null){
                return Bundle.format("history.config.default", findLocale(player.locale), name, ftime);
            }
            return Bundle.format("history.config.unit", findLocale(player.locale), name, icons.get(unit.name), ftime);
        }
        return Bundle.format("history.config.changed", findLocale(player.locale), name, ftime);
    }

    public static final StringMap icons = StringMap.of(
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
}
