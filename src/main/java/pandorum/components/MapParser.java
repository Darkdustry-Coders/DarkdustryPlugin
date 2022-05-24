package pandorum.components;

import arc.files.Fi;
import arc.graphics.Color;
import arc.struct.StringMap;
import arc.util.Log;
import arc.util.io.CounterInputStream;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.io.SaveIO;
import mindustry.io.SaveVersion;
import mindustry.maps.Map;
import mindustry.world.*;
import mindustry.world.blocks.environment.OreBlock;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

import static mindustry.Vars.*;
import static mindustry.io.MapIO.colorFor;
import static pandorum.util.Utils.getPluginFile;

public class MapParser {

    public static void load() {
        try {
            Fi colors = getPluginFile().child("block_colors.png");
            BufferedImage image = ImageIO.read(colors.read());
            for (Block block : content.blocks()) {
                block.mapColor.argb8888(block instanceof OreBlock ? block.itemDrop.color.argb8888() : image.getRGB(block.id, 0));
                block.mapColor.a = 1f;
            }
        } catch (Exception e) {
            Log.err(e);
        }
    }

    public static byte[] parseMap(Map map) {
        try {
            return parseImage(generatePreview(map));
        } catch (IOException e) {
            return new byte[0];
        }
    }

    public static byte[] parseTiles(Tiles tiles) {
        try {
            return parseImage(generatePreview(tiles));
        } catch (IOException e) {
            return new byte[0];
        }
    }

    public static BufferedImage generatePreview(Map map) throws IOException {
        return generatePreview(map.file.read(bufferSize));
    }

    public static BufferedImage generatePreview(InputStream input) throws IOException {
        try (InputStream ifs = new InflaterInputStream(input); CounterInputStream counter = new CounterInputStream(ifs); DataInputStream stream = new DataInputStream(counter)) {
            SaveIO.readHeader(stream);
            SaveVersion version = SaveIO.getSaveWriter(stream.readInt());
            StringMap meta = new StringMap();
            version.region("meta", stream, counter, data -> meta.putAll(version.readStringMap(data)));

            int width = meta.getInt("width"), height = meta.getInt("height");

            var floors = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            var walls = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            var fgraphics = floors.createGraphics();
            var shade = new java.awt.Color(0, 0, 0, 64);

            CachedTile tile = new CachedTile() {
                @Override
                public void setBlock(Block type) {
                    super.setBlock(type);

                    int color = colorFor(block(), Blocks.air, Blocks.air, team());
                    if (color != 255 && color != 0) {
                        walls.setRGB(x, floors.getHeight() - 1 - y, convert(color));
                        fgraphics.setColor(shade);
                        fgraphics.drawRect(x, floors.getHeight() - 1 - y + 1, 1, 1);
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
                public void begin() {
                    world.setGenerating(true);
                }

                @Override
                public void end() {
                    world.setGenerating(false);
                }

                @Override
                public void onReadBuilding() {
                    if (tile.build != null) {
                        int size = tile.block().size;
                        int offsetX = -(size - 1) / 2;
                        int offsetY = -(size - 1) / 2;
                        for (int dx = 0; dx < size; dx++) {
                            for (int dy = 0; dy < size; dy++) {
                                int drawx = tile.x + dx + offsetX, drawy = tile.y + dy + offsetY;
                                walls.setRGB(drawx, floors.getHeight() - 1 - drawy, tile.team().color.argb8888());
                            }
                        }
                    }
                }

                @Override
                public Tile tile(int index) {
                    tile.x = (short) (index % width);
                    tile.y = (short) (index / width);
                    return tile;
                }

                @Override
                public Tile create(int x, int y, int floorID, int overlayID, int wallID) {
                    if (overlayID != 0) {
                        floors.setRGB(x, floors.getHeight() - 1 - y, convert(colorFor(Blocks.air, Blocks.air, content.block(overlayID), Team.derelict)));
                    } else {
                        floors.setRGB(x, floors.getHeight() - 1 - y, convert(colorFor(Blocks.air, content.block(floorID), Blocks.air, Team.derelict)));
                    }
                    return tile;
                }
            }));

            fgraphics.drawImage(walls, 0, 0, null);
            fgraphics.dispose();
            return floors;
        } finally {
            content.setTemporaryMapper(null);
        }
    }

    public static BufferedImage generatePreview(Tiles tiles) {
        var image = new BufferedImage(tiles.width, tiles.height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < tiles.width; x++) {
            for (int y = 0; y < tiles.height; y++) {
                Tile tile = tiles.getc(x, y);
                image.setRGB(x, tiles.height - 1 - y, convert(colorFor(tile.block(), tile.floor(), tile.overlay(), tile.team())));
            }
        }
        return image;
    }

    public static byte[] parseImage(BufferedImage image) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", stream);
        return stream.toByteArray();
    }

    public static int convert(int color) {
        return new Color(color).argb8888();
    }
}
