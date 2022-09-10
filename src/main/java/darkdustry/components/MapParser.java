package darkdustry.components;

import arc.graphics.*;
import arc.graphics.PixmapIO.PngWriter;
import arc.util.io.*;
import darkdustry.DarkdustryPlugin;
import mindustry.content.Blocks;
import mindustry.ctype.*;
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

    public static byte[] renderMinimap() {
        return parseImage(MapIO.generatePreview(world.tiles));
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
            CachedTile cachedTile = new CachedTile() {
                @Override
                public void setBlock(Block type) {
                    super.setBlock(type);

                    int color = colorFor(block(), Blocks.air, Blocks.air, team());
                    if (color != black) {
                        walls.setRaw(x, floors.height - 1 - y, color);
                        floors.set(x, floors.height - 1 - y + 1, shade);
                    }
                }
            };

            var contents = new MappableContent[ContentType.all.length][0];

            version.region("content", stream, counter, input -> {
                for (int i = 0; i < stream.readByte(); i++) {
                    var type = ContentType.all[stream.readByte()];
                    contents[type.ordinal()] = new MappableContent[stream.readShort()];

                    for (int j = 0; j < contents[type.ordinal()].length; j++) {
                        String name = stream.readUTF();
                        contents[type.ordinal()][j] = content.getByName(type, SaveFileReader.fallback.get(name, name));
                    }
                }
            });

            var context = new WorldContext() {
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
                    if (cachedTile.build != null) {
                        int size = cachedTile.block().size;
                        int offsetx = -(size - 1) / 2;
                        int offsety = -(size - 1) / 2;
                        for (int dx = 0; dx < size; dx++) {
                            for (int dy = 0; dy < size; dy++) {
                                int drawx = cachedTile.x + dx + offsetx, drawy = cachedTile.y + dy + offsety;
                                walls.set(drawx, floors.height - 1 - drawy, cachedTile.build.team.color.rgba8888());
                            }
                        }
                    }
                }

                @Override
                public Tile tile(int index) {
                    cachedTile.x = (short) (index % map.width);
                    cachedTile.y = (short) (index / map.width);
                    return cachedTile;
                }

                @Override
                public Tile create(int x, int y, int floorID, int overlayID, int wallID) {
                    if (overlayID != 0) {
                        floors.set(x, floors.height - 1 - y, colorFor(Blocks.air, Blocks.air, content.block(overlayID), Team.derelict));
                    } else {
                        floors.set(x, floors.height - 1 - y, colorFor(Blocks.air, content.block(floorID), Blocks.air, Team.derelict));
                    }
                    return cachedTile;
                }
            };

            version.region("preview_map", stream, counter, input -> {
                int width = stream.readUnsignedShort();
                int height = stream.readUnsignedShort();

                context.resize(width, height);

                for (int i = 0; i < width * height; i++) {
                    int x = i % width, y = i / width;
                    short floorid = stream.readShort();
                    short oreid = stream.readShort();
                    int consecutives = stream.readUnsignedByte();

                    context.create(x, y, floorid, oreid, (short) 0);

                    for (int j = i + 1; j < i + 1 + consecutives; j++) {
                        int newx = j % width, newy = j / width;
                        context.create(newx, newy, floorid, oreid, (short) 0);
                    }

                    i += consecutives;
                }

                for (int i = 0; i < width * height; i++) {
                    Block block = (Block) contents[ContentType.block.ordinal()][(stream.readShort())];
                    Tile tile = context.tile(i);
                    if (block == null) block = Blocks.air;
                    boolean isCenter = true;
                    byte packedCheck = stream.readByte();
                    boolean hadEntity = (packedCheck & 1) != 0;
                    boolean hadData = (packedCheck & 2) != 0;

                    if (hadEntity) {
                        isCenter = stream.readBoolean();
                    }

                    if (isCenter) {
                        tile.setBlock(block);
                    }

                    if (hadEntity) {
                        if (isCenter) {
                            if (block.hasBuilding()) {
                                version.readChunk(stream, true, in -> {
                                    byte revision = in.readByte();
                                    tile.build.readAll(Reads.get(in), revision);
                                });
                            } else {
                                version.skipChunk(input);
                            }

                            context.onReadBuilding();
                        }
                    } else if (hadData) {
                        tile.setBlock(block);
                        tile.data = stream.readByte();
                    } else {
                        int consecutives = stream.readUnsignedByte();

                        for (int j = i + 1; j < i + 1 + consecutives; j++) {
                            context.tile(j).setBlock(block);
                        }

                        i += consecutives;
                    }
                }
            });

            floors.draw(walls, true);
            walls.dispose();
            return floors;
        }
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
