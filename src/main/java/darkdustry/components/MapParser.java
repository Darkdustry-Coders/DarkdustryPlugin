package darkdustry.components;

import arc.graphics.*;
import arc.graphics.PixmapIO.PngWriter;
import arc.util.io.CounterInputStream;
import darkdustry.DarkdustryPlugin;
import mindustry.io.SaveIO;
import mindustry.io.SaveVersion;
import mindustry.maps.Map;
import mindustry.world.Tile;
import mindustry.world.Tiles;
import mindustry.world.WorldContext;
import mindustry.world.blocks.environment.OreBlock;

import java.io.DataInputStream;
import java.io.IOException;
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
                if (block instanceof OreBlock) block.mapColor.set(block.itemDrop.color);
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
        try (
                var is = new InflaterInputStream(map.file.read(bufferSize));
                var counter = new CounterInputStream(is);
                var stream = new DataInputStream(counter)
        ) {
            SaveIO.readHeader(stream);
            SaveVersion ver = SaveIO.getSaveWriter(stream.readInt());

            Tiles tiles = new Tiles(map.width, map.height);

            ver.region("meta", stream, counter, ver::readStringMap);
            ver.region("content", stream, counter, ver::readContentHeader);
            ver.region("preview_map", stream, counter, in -> ver.readMap(in, new WorldContext() {
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
                    Tile tile = new Tile(x, y, floorID, overlayID, wallID);
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
