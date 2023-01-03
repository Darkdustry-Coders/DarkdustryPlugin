package darkdustry.features;

import arc.Events;
import arc.math.geom.Geometry;
import arc.struct.ObjectMap;
import darkdustry.utils.Find;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.world.Block;

import static arc.util.Strings.parseInt;
import static mindustry.Vars.*;

public class SchemeSize {

    /** Список id пользователей Scheme Size'а */
    public static final ObjectMap<Integer, String> SSUsers = new ObjectMap<>();

    public static void load() {
        Events.on(PlayerJoin.class, event -> Call.clientPacketReliable(event.player.con, "GiveYourPlayerData", null));
        Events.on(PlayerLeave.class, event -> SSUsers.remove(event.player.id));

        netServer.addPacketHandler("ThisIsMyPlayerData", (target, args) -> SSUsers.put(target.id, args.replace("|", "").replace("=", "")));
        netServer.addPacketHandler("GivePlayerDataPlease", (target, args) -> Call.clientPacketReliable(target.con, "ThisIsYourPlayerData", "S" + SSUsers.toString("|")));

        netServer.addPacketHandler("fill", (player, args) -> {
            try {
                if (player.admin) fill(args.split(" "));
            } catch (Exception ignored) {}
        });

        netServer.addPacketHandler("brush", (player, args) -> {
            try {
                if (player.admin) brush(args.split(" "));
            } catch (Exception ignored) {}
        });
    }

    private static void fill(String[] args) {
        Block floor = Find.block(args[0]), block = Find.block(args[1]), overlay = Find.block(args[2]);
        int cx = parseInt(args[3]), cy = parseInt(args[4]), width = parseInt(args[5]), height = parseInt(args[6]);

        for (int x = cx; x < cx + width; x++)
            for (int y = cy; y < cy + height; y++)
                edit(floor, block, overlay, x, y);
    }

    private static void brush(String[] args) {
        Block floor = Find.block(args[0]), block = Find.block(args[1]), overlay = Find.block(args[2]);
        int cx = parseInt(args[3]), cy = parseInt(args[4]), radius = parseInt(args[5]);

        Geometry.circle(cx, cy, radius, (x, y) -> edit(floor, block, overlay, x, y));
    }

    private static void edit(Block floor, Block block, Block overlay, int x, int y) {
        var tile = world.tile(x, y);
        if (tile == null) return;

        tile.setFloorNet(floor == null ? tile.floor() : floor, overlay == null ? tile.overlay() : overlay);
        if (block != null) tile.setNet(block);
    }
}