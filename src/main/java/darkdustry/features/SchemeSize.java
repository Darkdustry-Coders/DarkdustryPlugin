package darkdustry.features;

import arc.Events;
import arc.math.geom.Geometry;
import arc.struct.IntMap;
import darkdustry.utils.Find;
import mindustry.game.EventType.*;
import mindustry.gen.Call;
import mindustry.io.JsonIO;
import mindustry.world.Block;

import static arc.util.Strings.parseInt;
import static darkdustry.PluginVars.maxFillArea;
import static darkdustry.utils.Checks.invalidArea;
import static mindustry.Vars.*;

public class SchemeSize {

    /** Список игроков, использующих Scheme Size и их подписи. */
    public static final IntMap<String> signatures = new IntMap<>();

    public static void load() {
        Events.on(PlayerJoin.class, event -> Call.clientPacketReliable(event.player.con, "SendMeSubtitle", null));
        Events.on(PlayerLeave.class, event -> signatures.remove(event.player.id));

        netServer.addPacketHandler("MySubtitle", (player, text) -> {
            signatures.put(player.id, text);
            Call.clientPacketReliable("Subtitles", JsonIO.write(signatures));
        });

        netServer.addPacketHandler("fill", (player, text) -> {
            if (!player.admin) return;
            
            try {
                var args = text.split(" ");

                int cx = parseInt(args[3]), cy = parseInt(args[4]), width = parseInt(args[5]), height = parseInt(args[6]);
                if (invalidArea(player, width, height, maxFillArea)) return;

                Block floor = Find.block(args[0]), block = Find.block(args[1]), overlay = Find.block(args[2]);
                for (int x = cx; x < cx + width; x++) for (int y = cy; y < cy + height; y++) edit(floor, block, overlay, x, y);
            } catch (Exception ignored) {}
        });

        netServer.addPacketHandler("brush", (player, text) -> {
            if (!player.admin) return;

            try {
                var args = text.split(" ");
                
                int cx = parseInt(args[3]), cy = parseInt(args[4]), radius = parseInt(args[5]);
                if (invalidArea(player, radius, maxFillArea)) return;

                Block floor = Find.block(args[0]), block = Find.block(args[1]), overlay = Find.block(args[2]);
                Geometry.circle(cx, cy, radius, (x, y) -> edit(floor, block, overlay, x, y));
            } catch (Exception ignored) {}
        });
    }

    private static void edit(Block floor, Block block, Block overlay, int x, int y) {
        var tile = world.tile(x, y);
        if (tile == null) return;

        tile.setFloorNet(floor == null ? tile.floor() : floor, overlay == null ? tile.overlay() : overlay);
        if (block != null) tile.setNet(block);
    }
}