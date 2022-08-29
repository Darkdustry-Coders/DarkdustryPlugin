package darkdustry.features;

import arc.Events;
import arc.math.geom.Geometry;
import arc.struct.ObjectMap;
import darkdustry.utils.Find;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.world.Block;

import static arc.util.Strings.parseInt;
import static darkdustry.utils.Checks.invalidFillAmount;
import static mindustry.Vars.*;

public class SchemeSize {

    /** Список id пользователей Scheme Size'а */
    public static final ObjectMap<Integer, String> SSUsers = new ObjectMap<>();

    public static void load() {
        Events.on(PlayerJoin.class, event -> Call.clientPacketReliable(event.player.con, "GiveYourPlayerData", null));
        Events.on(PlayerLeave.class, event -> SSUsers.remove(event.player.id));

        netServer.addPacketHandler("ThisIsMyPlayerData", (player, args) -> SSUsers.put(player.id, args.replace("#", "").replace("=", "")));
        netServer.addPacketHandler("GivePlayerDataPlease", (player, args) -> Call.clientPacketReliable(player.con, "ThisIsYourPlayerData", SSUsers.toString("#")));

        // всё то, что дальше, не нужно для интеграции сервера с модом
        // оно просто перенесёно сюда, чтобы не захламлять AdminCommands

        netServer.addPacketHandler("fill", (player, args) -> {
            try {
                if (player.admin) fill(player, args.split(" "));
            } catch (Exception ignored) {}
        });

        netServer.addPacketHandler("brush", (player, args) -> {
            try {
                if (player.admin) brush(player, args.split(" "));
            } catch (Exception ignored) {}
        });
    }

    private static void fill(Player player, String[] args) {
        int sx = parseInt(args[3]), sy = parseInt(args[4]), width = parseInt(args[5]), height = parseInt(args[6]);
        if (invalidFillAmount(player, width, height)) return;

        Block floor = Find.block(args[0]), block = Find.block(args[1]), overlay = Find.block(args[2]);
        for (int x = sx; x < sx + width; x++)
            for (int y = sy; y < sy + height; y++)
                edit(floor, block, overlay, x, y);
    }

    private static void brush(Player player, String[] args) {
        int sx = parseInt(args[3]), sy = parseInt(args[4]), radius = parseInt(args[5]);
        if (invalidFillAmount(player, radius)) return;

        Block floor = Find.block(args[0]), block = Find.block(args[1]), overlay = Find.block(args[2]);
        Geometry.circle(sx, sy, radius, (x, y) -> edit(floor, block, overlay, x, y));
    }

    private static void edit(Block floor, Block block, Block overlay, int x, int y) {
        var tile = world.tile(x, y);
        if (tile == null) return;

        tile.setFloorNet(floor == null ? tile.floor() : floor, overlay == null ? tile.overlay() : overlay);
        if (block != null) tile.setNet(block);
    }
}
