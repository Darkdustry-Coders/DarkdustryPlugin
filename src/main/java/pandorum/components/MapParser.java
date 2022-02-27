package pandorum.components;

import arc.graphics.Pixmap;
import arc.graphics.PixmapIO.PngWriter;
import arc.util.Log;
import mindustry.io.MapIO;
import mindustry.maps.Map;
import mindustry.world.Tiles;
import mindustry.world.blocks.environment.OreBlock;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static mindustry.Vars.content;

public class MapParser {

    public static void init() {
        try {
            BufferedImage image = ImageIO.read(new File("../block_colors.png"));

            content.blocks().each(block -> {
                block.mapColor.argb8888(image.getRGB(block.id, 0));
                if (block instanceof OreBlock) {
                    block.mapColor.set(block.itemDrop.color);
                }
            });
        } catch (IOException e) {
            Log.err(e);
        }
    }

    public static byte[] parseMap(Map map) {
        try {
            return parseMap(MapIO.generatePreview(map));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] parseMap(Tiles tiles) {
        try {
            return parseMap(MapIO.generatePreview(tiles));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] parseMap(Pixmap pixmap) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PngWriter writer = new PngWriter((int) (pixmap.width * pixmap.height * 1.5f));
        try {
            writer.setFlipY(false);
            writer.write(stream, pixmap);
            return stream.toByteArray();
        } finally {
            writer.dispose();
        }
    }
}
