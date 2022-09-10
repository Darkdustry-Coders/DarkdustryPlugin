package darkdustry.components;

import arc.graphics.*;
import arc.graphics.PixmapIO.PngWriter;
import arc.util.io.CounterInputStream;
import darkdustry.DarkdustryPlugin;
import mindustry.io.SaveIO;
import mindustry.maps.Map;
import mindustry.world.*;

import java.io.*;
import java.util.zip.InflaterInputStream;

import static arc.util.io.Streams.*;
import static darkdustry.utils.Utils.getPluginResource;
import static mindustry.Vars.*;
import static mindustry.io.MapIO.generatePreview;

public class MapParser {

    public static void load() {
        try {
            var pixmap = PixmapIO.readPNG(getPluginResource("block_colors.png"));

            for (int i = 0; i < pixmap.width; i++) {
                var block = content.block(i);
                if (block.itemDrop != null) block.mapColor.set(block.itemDrop.color);
                else block.mapColor.rgba8888(pixmap.get(i, 0)).a(1f);
            }

            pixmap.dispose();

            DarkdustryPlugin.info("Loaded @ block colors.", pixmap.width);
        } catch (Exception e) {
            DarkdustryPlugin.error("Error reading file block_colors.png: @", e);
        }
    }

    public static byte[] renderMap(Map map) {
        try {
            return parseImage(generatePreview(parseTiles(map)));
        } catch (Exception e) {
            return emptyBytes;
        }
    }

    public static byte[] renderMinimap() {
        return parseImage(generatePreview(world.tiles));
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

    public static Tiles parseTiles(Map map) throws IOException {
        try (var counter = new CounterInputStream(new InflaterInputStream(map.file.read(bufferSize))); var stream = new DataInputStream(counter)) {
            SaveIO.readHeader(stream);
            var version = SaveIO.getSaveWriter(stream.readInt());

            var tiles = new Tiles(map.width, map.height);

            version.region("meta", stream, counter, version::readStringMap);
            version.region("content", stream, counter, version::readContentHeader);
            version.region("preview_map", stream, counter, input -> version.readMap(input, new WorldContext() {
                @Override public void resize(int width, int height) {}
                @Override public boolean isGenerating() { return false; }
                @Override public void begin() {}
                @Override public void end() {}

                @Override
                public Tile tile(int index) {
                    return tiles.geti(index);
                }

                @Override
                public Tile create(int x, int y, int floorID, int overlayID, int wallID) {
                    var tile = new Tile(x, y, floorID, overlayID, wallID);
                    tiles.set(x, y, tile);
                    return tile;
                }
            }));

            return tiles;
        } finally {
            content.setTemporaryMapper(null);
        }
    }
}
