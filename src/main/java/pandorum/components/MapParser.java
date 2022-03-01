package pandorum.components;

import arc.graphics.Color;
import arc.util.Log;
import arc.util.io.CounterInputStream;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.io.MapIO;
import mindustry.io.SaveIO;
import mindustry.io.SaveVersion;
import mindustry.maps.Map;
import mindustry.world.*;
import mindustry.world.blocks.environment.OreBlock;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.zip.InflaterInputStream;

import static mindustry.Vars.*;

public class MapParser {

    public static void init() {
        try {
            BufferedImage image = ImageIO.read(new File("../block_colors.png"));
            content.blocks().each(block -> block.mapColor.argb8888(block instanceof OreBlock ? block.itemDrop.color.argb8888() : image.getRGB(block.id, 0)));
        } catch (IOException e) {
            Log.err(e);
        }
    }

    public static byte[] parseMap(Map map) {
        try {
            return parseImage(generatePreview(map));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] parseTiles(Tiles tiles) {
        try {
            return parseImage(generatePreview(tiles));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage generatePreview(Map map) throws IOException {
        try (InputStream ifs = new InflaterInputStream(map.file.read(bufferSize)); CounterInputStream counter = new CounterInputStream(ifs); DataInputStream stream = new DataInputStream(counter)) {
            SaveIO.readHeader(stream);
            int version = stream.readInt();
            SaveVersion ver = SaveIO.getSaveWriter(version);
            ver.region("meta", stream, counter, ver::readStringMap);

            int width = map.width, height = map.height;

            var floors = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            var walls = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            var fgraphics = floors.createGraphics();
            var shade = new java.awt.Color(0, 0, 0, 64);
            int black = 255;

            CachedTile tile = new CachedTile() {
                @Override
                public void setBlock(Block type) {
                    super.setBlock(type);

                    int color = MapIO.colorFor(block(), Blocks.air, Blocks.air, team());
                    if (color != black && color != 0) {
                        walls.setRGB(x, floors.getHeight() - 1 - y, conv(color));
                        fgraphics.setColor(shade);
                        fgraphics.drawRect(x, floors.getHeight() - 1 - y + 1, 1, 1);
                    }
                }
            };

            ver.region("content", stream, counter, ver::readContentHeader);
            ver.region("preview_map", stream, counter, in -> ver.readMap(in, new WorldContext() {
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
                        int c = tile.build.team.color.argb8888();
                        int size = tile.block().size;
                        int offsetx = -(size - 1) / 2;
                        int offsety = -(size - 1) / 2;
                        for (int dx = 0; dx < size; dx++) {
                            for (int dy = 0; dy < size; dy++) {
                                int drawx = tile.x + dx + offsetx, drawy = tile.y + dy + offsety;
                                walls.setRGB(drawx, floors.getHeight() - 1 - drawy, c);
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
                        floors.setRGB(x, floors.getHeight() - 1 - y, conv(MapIO.colorFor(Blocks.air, Blocks.air, content.block(overlayID), Team.derelict)));
                    } else {
                        floors.setRGB(x, floors.getHeight() - 1 - y, conv(MapIO.colorFor(Blocks.air, content.block(floorID), Blocks.air, Team.derelict)));
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
        for(int x = 0; x < tiles.width; x++){
            for(int y = 0; y < tiles.height; y++){
                Tile tile = tiles.getn(x, y);
                image.setRGB(x, tiles.height - 1 - y, conv(MapIO.colorFor(tile.block(), tile.floor(), tile.overlay(), tile.team())));
            }
        }
        return image;
    }

    public static byte[] parseImage(BufferedImage image) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", stream);
        return stream.toByteArray();
    }

    public static int conv(int rgba) {
        return new Color().set(rgba).argb8888();
    }
}
