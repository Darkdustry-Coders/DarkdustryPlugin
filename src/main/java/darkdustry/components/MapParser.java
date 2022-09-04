package darkdustry.components;

import arc.graphics.Pixmap;
import arc.graphics.PixmapIO.PngWriter;
import darkdustry.DarkdustryPlugin;
import mindustry.io.MapIO;
import mindustry.maps.Map;
import mindustry.world.blocks.environment.OreBlock;

import static arc.util.io.Streams.*;
import static darkdustry.utils.Utils.getPluginResource;
import static mindustry.Vars.*;

public class MapParser {

    public static void load() {
        try {
            var pixmap = new Pixmap(getPluginResource("block_colors.png"));
            for (int i = 0; i < pixmap.width; i++) {
                var block = content.block(i);
                if (block instanceof OreBlock) block.mapColor.set(block.itemDrop.color);
                else block.mapColor.rgba8888(pixmap.get(i, 0)).a(1f);
            }

            pixmap.dispose();

            DarkdustryPlugin.info("Loaded @ block colors.", pixmap.width);
        } catch (Exception e) {
            DarkdustryPlugin.error("File block_colors.png is not found or corrupt: @", e);
        }
    }

    public static byte[] renderMap(Map map) {
        try {
            return parseImage(MapIO.generatePreview(map));
        } catch (Exception e) {
            return emptyBytes;
        }
    }

    public static byte[] renderMinimap() {
        return parseImage(MapIO.generatePreview(world.tiles));
    }

    public static byte[] parseImage(Pixmap pixmap) {
        var writer = new PngWriter(pixmap.width * pixmap.height);
        var stream = new OptimizedByteArrayOutputStream(pixmap.width * pixmap.height);

        try {
            writer.setFlipY(false);
            writer.write(stream, pixmap);
            return stream.toByteArray();
        } catch (Exception e) {
            return emptyBytes;
        } finally {
            writer.dispose();
        }
    }
}
