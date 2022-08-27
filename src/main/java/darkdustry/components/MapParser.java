package darkdustry.components;

import arc.graphics.Pixmap;
import arc.graphics.PixmapIO.PngWriter;
import mindustry.io.MapIO;
import mindustry.maps.Map;
import mindustry.world.blocks.environment.OreBlock;
import darkdustry.DarkdustryPlugin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static mindustry.Vars.*;
import static darkdustry.utils.Utils.*;

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
        } catch (Exception exception) {
            DarkdustryPlugin.error("File block_colors.png is not found or corrupt: @", exception);
        }
    }

    public static byte[] mapImage(Map map) {
        try {
            return parseImage(MapIO.generatePreview(map));
        } catch (IOException e) {
            return new byte[0];
        }
    }

    public static byte[] minimapImage() {
        return parseImage(MapIO.generatePreview(world.tiles));
    }

    public static byte[] parseImage(Pixmap pixmap) {
        var writer = new PngWriter(pixmap.width * pixmap.height);
        var stream = new ByteArrayOutputStream();

        try {
            writer.setFlipY(false);
            writer.write(stream, pixmap);
            return stream.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        } finally {
            writer.dispose();
        }
    }
}
