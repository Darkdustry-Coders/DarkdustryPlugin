package pandorum.components;

import arc.files.Fi;
import arc.graphics.Pixmap;
import arc.graphics.PixmapIO.PngWriter;
import arc.util.Log;
import mindustry.io.MapIO;
import mindustry.maps.Map;
import mindustry.world.Block;
import mindustry.world.Tiles;
import mindustry.world.blocks.environment.OreBlock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static mindustry.Vars.content;
import static pandorum.util.Utils.getPluginResource;

public class MapParser {

    public static void load() {
        Fi colors = getPluginResource("block_colors.png");

        try {
            Pixmap pixmap = new Pixmap(colors);
            for (int i = 0; i < pixmap.width; i++) {
                Block block = content.block(i);
                if (block instanceof OreBlock) {
                    block.mapColor.set(block.itemDrop.color);
                } else {
                    block.mapColor.rgba8888(pixmap.get(i, 0)).a(1f);
                }
            }

            Log.infoTag("Darkdustry", "Загружено цветов блоков: " + pixmap.width);
        } catch (Exception e) {
            Log.errTag("Darkdustry", "Файл 'block_colors.png' не найден или повреждён: " + e);
        }
    }

    public static byte[] parseMap(Map map) {
        try {
            return parseImage(MapIO.generatePreview(map));
        } catch (IOException e) {
            return new byte[0];
        }
    }

    public static byte[] parseTiles(Tiles tiles) {
        return parseImage(MapIO.generatePreview(tiles));
    }

    public static byte[] parseImage(Pixmap pixmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PngWriter writer = new PngWriter(pixmap.width * pixmap.height);
        writer.setFlipY(false);

        try {
            writer.write(stream, pixmap);
            return stream.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        } finally {
            writer.dispose();
        }
    }
}
