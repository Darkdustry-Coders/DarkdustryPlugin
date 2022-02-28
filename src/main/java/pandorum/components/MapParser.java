package pandorum.components;

import arc.graphics.Pixmap;
import arc.graphics.PixmapIO.PngWriter;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
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
            Draw.scl = 0.25f;
            Lines.useLegacyLine = true;
            BufferedImage image = ImageIO.read(new File("../block_colors.png"));
            content.blocks().each(block -> block.mapColor.argb8888(block instanceof OreBlock ? block.itemDrop.color.argb8888() : image.getRGB(block.id, 0)));
        } catch (IOException e) {
            Log.err(e);
        }
    }

    public static byte[] parseMap(Map map) {
        try {
            return parseImage(MapIO.generatePreview(map));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] parseTiles(Tiles tiles) {
        try {
            return parseImage(MapIO.generatePreview(tiles));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] parseImage(Pixmap pixmap) throws IOException {
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
