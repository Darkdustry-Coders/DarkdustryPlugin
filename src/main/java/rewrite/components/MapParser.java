package rewrite.components;

import arc.graphics.Pixmap;
import arc.graphics.PixmapIO.PngWriter;
import mindustry.io.MapIO;
import mindustry.maps.Map;
import mindustry.world.Block;
import mindustry.world.Tiles;
import mindustry.world.blocks.environment.OreBlock;
import rewrite.DarkdustryPlugin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static mindustry.Vars.*;
import static rewrite.utils.Utils.*;

public class MapParser {

    public static void load() {
        try {
            Pixmap pixmap = new Pixmap(getPluginResource("block_colors.png"));
            for (int i = 0; i < pixmap.width; i++) {
                Block block = content.block(i);
                if (block instanceof OreBlock) block.mapColor.set(block.itemDrop.color);
                else block.mapColor.rgba8888(pixmap.get(i, 0)).a(1f);
            }

            DarkdustryPlugin.info("Загружено @ цветов блоков.", pixmap.width);
        } catch (Exception exception) {
            DarkdustryPlugin.error("Файл block_colors.png не найден или повреждён: ", exception);
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
