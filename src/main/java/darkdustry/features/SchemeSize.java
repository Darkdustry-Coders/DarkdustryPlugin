package darkdustry.features;

import arc.Events;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.struct.IntMap;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Timer;
import arc.util.Timer.Task;
import darkdustry.utils.Find;
import mindustry.game.Team;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.world.Block;

import static arc.util.Strings.parseInt;
import static darkdustry.PluginVars.maxFillArea;
import static darkdustry.utils.Checks.invalidArea;
import static mindustry.Vars.*;

public class SchemeSize {

    /** Список id пользователей Scheme Size. */
    public static final ObjectMap<Integer, String> SSUsers = new ObjectMap<>();
    /** Список людей в радужной команде. */
    public static final IntMap<Task> rainbows = new IntMap<>();
    /** Команды, рассортированные по оттенку. */
    public static Seq<Team> rainbow;

    public static void load() {
        Events.on(PlayerJoin.class, event -> Call.clientPacketReliable(event.player.con, "GiveYourPlayerData", null));
        Events.on(PlayerLeave.class, event -> SSUsers.remove(event.player.id));

        netServer.addPacketHandler("ThisIsMyPlayerData", (target, args) -> SSUsers.put(target.id, args.replace("|", "").replace("=", "")));
        netServer.addPacketHandler("GivePlayerDataPlease", (target, args) -> Call.clientPacketReliable(target.con, "ThisIsYourPlayerData", "S" + SSUsers.toString("|")));

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

        netServer.addPacketHandler("rainbow", (player, args) -> {
            if (!player.admin) return;

            var target = Find.player(args.trim());
            if (target == null) return;

            var task = Timer.schedule(() -> target.team(rainbow.get(Mathf.floor(Time.time / 6f % rainbow.size))), 0f, .1f);
            rainbows.put(target.id, task);
        });

        rainbow = new Seq<>(Team.all);
        rainbow.filter(team -> {
            int[] hsv = Color.RGBtoHSV(team.color);
            return hsv[2] > 85;
        }).sort(team -> {
            int[] hsv = Color.RGBtoHSV(team.color);
            return hsv[0] * 1000 + hsv[1];
        });
    }

    private static void fill(Player player, String[] args) {
        int cx = parseInt(args[3]), cy = parseInt(args[4]), width = parseInt(args[5]), height = parseInt(args[6]);
        if (invalidArea(player, width, height, maxFillArea)) return;

        Block floor = Find.block(args[0]), block = Find.block(args[1]), overlay = Find.block(args[2]);
        for (int x = cx; x < cx + width; x++)
            for (int y = cy; y < cy + height; y++)
                edit(floor, block, overlay, x, y);
    }

    private static void brush(Player player, String[] args) {
        int cx = parseInt(args[3]), cy = parseInt(args[4]), radius = parseInt(args[5]);
        if (invalidArea(player, radius, maxFillArea)) return;

        Block floor = Find.block(args[0]), block = Find.block(args[1]), overlay = Find.block(args[2]);
        Geometry.circle(cx, cy, radius, (x, y) -> edit(floor, block, overlay, x, y));
    }

    private static void edit(Block floor, Block block, Block overlay, int x, int y) {
        var tile = world.tile(x, y);
        if (tile == null) return;

        tile.setFloorNet(floor == null ? tile.floor() : floor, overlay == null ? tile.overlay() : overlay);
        if (block != null) tile.setNet(block);
    }
}