package darkdustry.components;

import arc.graphics.*;
import arc.graphics.PixmapIO.PngWriter;
import arc.util.io.CounterInputStream;
import darkdustry.DarkdustryPlugin;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.io.*;
import mindustry.maps.Map;
import mindustry.world.*;
import mindustry.world.blocks.environment.OreBlock;

import java.io.*;
import java.util.zip.InflaterInputStream;

import static arc.util.io.Streams.*;
import static darkdustry.utils.Utils.getPluginResource;
import static mindustry.Vars.*;
import static mindustry.io.MapIO.colorFor;

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
            return parseImage(generatePreview(map));
        } catch (Exception e) {
            return emptyBytes;
        }
    }

    public static Pixmap generatePreview(Map map) throws IOException {
        try (CounterInputStream counter = new CounterInputStream(new InflaterInputStream(map.file.read(bufferSize))); DataInputStream stream = new DataInputStream(counter)) {
            SaveIO.readHeader(stream);
            SaveVersion version = SaveIO.getSaveWriter(stream.readInt());
            version.region("meta", stream, counter, version::readStringMap);

            Pixmap floors = new Pixmap(map.width, map.height);
            Pixmap walls = new Pixmap(map.width, map.height);
            int black = 255;
            int shade = Color.rgba8888(0f, 0f, 0f, 0.5f);
            CachedTile tile = new CachedTile() {
                @Override
                public void setBlock(Block type) {
                    super.setBlock(type);

                    int c = colorFor(block(), Blocks.air, Blocks.air, team());
                    if (c != black) {
                        walls.setRaw(x, floors.height - 1 - y, c);
                        floors.set(x, floors.height - 1 - y + 1, shade);
                    }
                }
            };

            version.region("content", stream, counter, version::readContentHeader);
            version.region("preview_map", stream, counter, in -> version.readMap(in, new WorldContext() {
                @Override
                public void resize(int width, int height) {}

                @Override
                public boolean isGenerating() {
                    return false;
                }

                @Override
                public void begin() {}

                @Override
                public void end() {}

                @Override
                public void onReadBuilding() {
                    if (tile.build != null) {
                        int color = tile.build.team.color.rgba8888();
                        int size = tile.block().size;
                        int offsetx = -(size - 1) / 2;
                        int offsety = -(size - 1) / 2;
                        for (int x = 0; x < size; x++) {
                            for (int y = 0; y < size; y++) {
                                int drawx = tile.x + x + offsetx, drawy = tile.y + y + offsety;
                                walls.set(drawx, floors.height - 1 - drawy, color);
                            }
                        }
                    }
                }

                @Override
                public Tile tile(int index) {
                    tile.x = (short) (index % map.width);
                    tile.y = (short) (index / map.width);
                    return tile;
                }

                @Override
                public Tile create(int x, int y, int floorID, int overlayID, int wallID) {
                    if (overlayID != 0) {
                        floors.set(x, floors.height - 1 - y, colorFor(Blocks.air, Blocks.air, content.block(overlayID), Team.derelict));
                    } else {
                        floors.set(x, floors.height - 1 - y, colorFor(Blocks.air, content.block(floorID), Blocks.air, Team.derelict));
                    }
                    return tile;
                }
            }));

            floors.draw(walls, true);
            walls.dispose();
            return floors;
        } finally {
            content.setTemporaryMapper(null);
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
