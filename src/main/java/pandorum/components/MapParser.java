package pandorum.components;

import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.PixmapIO.PngWriter;
import mindustry.io.MapIO;
import mindustry.maps.Map;
import mindustry.world.Tiles;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static mindustry.Vars.content;

public class MapParser {

    public static void init() {
        content.blocks().each(block -> block.mapColor.set(Color.cyan));
    }

    public static byte[] parseMap(Map map) {
        try {
            return parseMap(MapIO.generatePreview(map));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] parseMap(Tiles tiles) {
        try {
            return parseMap(MapIO.generatePreview(tiles));
        } catch (Exception e) {
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
