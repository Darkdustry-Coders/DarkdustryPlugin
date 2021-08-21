package pandorum.entry;

import static mindustry.Vars.world;
import static pandorum.Misc.colorizedName;
import static pandorum.Misc.findLocale;

import arc.struct.StringMap;
import arc.math.geom.Point2;
import mindustry.content.Blocks;
import mindustry.entities.units.UnitCommand;
import mindustry.game.EventType.ConfigEvent;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.Tile;
import pandorum.comp.Bundle;

import java.util.TimeZone;
import java.time.ZoneId;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConfigEntry implements HistoryEntry{
    public String name;
    public Block block;
    public Object value;
    public boolean connect;
    public Building build;
    public Date time;

    public ConfigEntry(ConfigEvent event, boolean connect){
        this.name = Groups.player.contains(p -> event.player == p) ? colorizedName(event.player) : Bundle.get("events.unknown", Bundle.defaultLocale());
        this.block = event.tile.block();
        this.value = event.value;
        this.connect = connect;
        this.build = event.tile;
        this.time = new Date();
    }

    @Override
    public String getMessage(Player player){
        final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Moscow")));
        final String ftime = df.format(this.time);

        if (block.configurations.containsKey(Integer.class) && block.configurations.containsKey(Point2[].class)) {
            int data = (int) value;
            Tile tile = world.tile(data);
            if (tile == null) {
                return Bundle.get("events.history.unknown", findLocale(player.locale));
            }

            if (connect) {
                return Bundle.format("events.history.config.power-node.connect", findLocale(player.locale), name, block, tile.x, tile.y, ftime);
            }

            return Bundle.format("events.history.config.power-node.disconnect", findLocale(player.locale), name, block, tile.x, tile.y, ftime);
        }

        if(block == Blocks.door || block == Blocks.doorLarge){
            boolean data = (boolean)value;
            return data ? Bundle.format("events.history.config.door.on", findLocale(player.locale), name, block, ftime) : Bundle.format("events.history.config.door.off", findLocale(player.locale), name, block, ftime);
        }

        if(block == Blocks.switchBlock){
            boolean data = (boolean)value;
            return data ? Bundle.format("events.history.config.switch.on", findLocale(player.locale), name, ftime) : Bundle.format("events.history.config.switch.off", findLocale(player.locale), name, ftime);
        }

        if(block == Blocks.commandCenter){
            final String[] commands = Bundle.get("events.history.config.command-center.all", findLocale(player.locale)).split(", ");
            return Bundle.format("events.history.config.command-center", findLocale(player.locale), name, commands[((UnitCommand)value).ordinal()], ftime);
        }

        if(block == Blocks.liquidSource){
            Liquid liquid = (Liquid)value;
            if(liquid == null){
                return Bundle.format("events.history.config.default", findLocale(player.locale), name, ftime);
            }

            return Bundle.format("events.history.config", findLocale(player.locale), name, icons.get(liquid.name), ftime);
        }

        if(block == Blocks.unloader || block == Blocks.sorter || block == Blocks.invertedSorter || block == Blocks.itemSource){
            Item item = (Item)value;
            if(item == null){
                return Bundle.format("events.history.config.default", findLocale(player.locale), name, ftime);
            }

            return Bundle.format("events.history.config", findLocale(player.locale), name, icons.get(item.name), ftime);
        }

        if(block == Blocks.navalFactory || block == Blocks.airFactory || block == Blocks.groundFactory || block == Blocks.blockForge || block == Blocks.payloadSource){
            return Bundle.format("events.history.config.changed", findLocale(player.locale), name, ftime);
        }
        return Bundle.get("events.history.unknown", findLocale(player.locale)); // не ну а че
    }

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
