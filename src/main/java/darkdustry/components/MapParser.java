package darkdustry.components;

import arc.func.Prov;
import arc.graphics.*;
import arc.graphics.PixmapIO.PngWriter;
import arc.util.io.CounterInputStream;
import darkdustry.DarkdustryPlugin;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.io.*;
import mindustry.maps.Map;
import mindustry.world.*;

import java.io.*;
import java.util.zip.InflaterInputStream;

import static arc.graphics.Color.blackRgba;
import static arc.util.io.Streams.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;
import static mindustry.io.MapIO.colorFor;

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
            return parseImage(generatePreview(map), true);
        } catch (Exception e) {
            return emptyBytes;
        }
    }

    public static byte[] renderMinimap() {
        return parseImage(MapIO.generatePreview(world.tiles), false);
    }

    public static byte[] parseImage(Pixmap pixmap, boolean flip) {
        var writer = new PngWriter(pixmap.width * pixmap.height);
        var stream = new OptimizedByteArrayOutputStream(pixmap.width * pixmap.height);

        try {
            writer.setFlipY(flip);
            writer.write(stream, pixmap);
            return stream.toByteArray();
        } catch (Exception e) {
            return emptyBytes;
        } finally {
            writer.dispose();
        }
    }

    private static Pixmap generatePreview(Map map) throws IOException {
        try (var counter = new CounterInputStream(new InflaterInputStream(map.file.read(bufferSize))); var stream = new DataInputStream(counter)) {
            SaveIO.readHeader(stream);

            var version = new FixedSave(stream.readInt());

            var pixmap = new Pixmap(map.width, map.height);
            var tile = new ContainerTile() {
                @Override
                public void setBlock(Block block) {
                    super.setBlock(block);

                    int color = colorFor(block, Blocks.air, Blocks.air, notNullElse(team, Team.derelict));
                    if (color != blackRgba) pixmap.set(x, y, color);
                }
            };

            version.region("meta", stream, counter, version::readStringMap);
            version.region("content", stream, counter, version::readContentHeader);
            version.region("preview_map", stream, counter, input -> version.readMap(input, new WorldContext() {
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
                    if (tile.team == null) return;

                    int size = tile.block().size;
                    int offset = -(size - 1) / 2;

                    for (int x = 0; x < size; x++)
                        for (int y = 0; y < size; y++)
                            pixmap.set(tile.x + x + offset, tile.y + y + offset, tile.team.color.rgba8888());
                }

                @Override
                public Tile tile(int index) {
                    tile.x = (short) (index % map.width);
                    tile.y = (short) (index / map.width);
                    return tile;
                }

                @Override
                public Tile create(int x, int y, int floorID, int overlayID, int wallID) {
                    pixmap.set(x, y, colorFor(Blocks.air, content.block(floorID), content.block(overlayID), Team.derelict));
                    return null;
                }
            }));

            return pixmap;
        }
    }

    public static class ContainerTile extends CachedTile {
        public Team team;

        @Override
        public void setTeam(Team team) {
            this.team = team;
        }

        @Override
        public void setBlock(Block block) {
            this.block = block;
        }

        @Override
        protected void changeBuild(Team team, Prov<Building> entityprov, int rotation) {}

        @Override
        protected void changed() {}
    }

    public static class FixedSave extends SaveVersion {

        public FixedSave(int version) {
            super(version);
        }

        @Override
        public void readMap(DataInput stream, WorldContext context) throws IOException {
            int width = stream.readUnsignedShort();
            int height = stream.readUnsignedShort();

            for (int i = 0; i < width * height; i++) {
                short floorID = stream.readShort();
                short oreID = stream.readShort();

                int consecutive = stream.readUnsignedByte();

                for (int j = i; j <= i + consecutive; j++)
                    context.create(j % width, j / width, floorID, oreID, 0);

                i += consecutive;
            }

            for (int i = 0; i < width * height; i++) {
                var block = notNullElse(content.block(stream.readShort()), Blocks.air);
                var tile = context.tile(i);

                byte packedCheck = stream.readByte();
                boolean hadEntity = (packedCheck & 1) != 0,
                        hadData = (packedCheck & 2) != 0,
                        isCenter = !hadEntity || stream.readBoolean();

                if (isCenter || hadData) {
                    tile.setBlock(block);
                }

                if (hadEntity) {
                    if (!isCenter) continue;

                    if (block.hasBuilding())
                        readChunk(stream, true, input -> {
                            input.skipBytes(6);
                            tile.setTeam(Team.get(input.readByte()));
                            input.skipBytes(lastRegionLength - 7);
                        });
                    else skipChunk(stream, true);

                    context.onReadBuilding();
                } else {
                    int consecutive = stream.readUnsignedByte();

                    for (int j = i + 1; j <= i + consecutive; j++)
                        context.tile(j).setBlock(block);

                    i += consecutive;
                }
            }
        }
    }
}