package darkdustry.components;

import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.PixmapIO;
import arc.graphics.PixmapIO.PngWriter;
import arc.math.Mathf;
import arc.struct.IntMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Pack;
import arc.util.io.CounterInputStream;
import darkdustry.DarkdustryPlugin;
import mindustry.content.Blocks;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.ctype.MappableContent;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.io.MapIO;
import mindustry.io.SaveIO;
import mindustry.io.SaveVersion;
import mindustry.maps.Map;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.WorldContext;
import mindustry.world.blocks.storage.CoreBlock;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.InflaterInputStream;

import static arc.util.io.Streams.OptimizedByteArrayOutputStream;
import static arc.util.io.Streams.emptyBytes;
import static darkdustry.utils.Utils.getPluginResource;
import static mindustry.Vars.*;
import static mindustry.io.MapIO.colorFor;
import static mindustry.io.MapIO.generatePreview;

public class MapParser {
    private static final IntMap<FixedSave> versions = new IntMap<>();
    private static final Seq<FixedSave> versionArray = Seq.with(
            new FixedLegacySave(1), new FixedLegacySave(2), new FixedLegacySave(3),
            new FixedSave(4), new FixedSave(5), new FixedSave(6),
            new FixedSave(7));

    static {
        for (FixedSave version : versionArray) {
            versions.put(version.version, version);
        }
    }

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
            return parseImage(generatePreview(map));
        } catch (Exception e) {
            Log.err("Failed to generated preview for map: " + map.name(), e);
            return emptyBytes;
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

    private static FixedSave getSaveWriter(int version) {
        return versions.get(version);
    }

    private static Pixmap generatePreview(Map map) throws IOException {
        try (var counter = new CounterInputStream(new InflaterInputStream(map.file.read(bufferSize)));
             var stream = new DataInputStream(counter)) {
            SaveIO.readHeader(stream);
            var version = getSaveWriter(stream.readInt());

            Pixmap floors = new Pixmap(map.width, map.height);
            Pixmap walls = new Pixmap(map.width, map.height);
            int black = 255;
            int shade = Color.rgba8888(0f, 0f, 0f, 0.5f);
            ContainerTile current = new ContainerTile() {
                @Override
                public void setBlock(Block type) {
                    super.setBlock(type);

                    int c = colorFor(type, Blocks.air, Blocks.air, team != null ? team : Team.derelict);
                    if (c != black) {
                        walls.setRaw(x, floors.height - 1 - y, c);
                        floors.set(x, floors.height - 1 - y + 1, shade);
                    }
                }
            };

            version.region("meta", stream, counter, version::readStringMap);
            version.region("content", stream, counter, version::readContentHeader);
            version.region("preview_map", stream, counter, input -> version.readMap(input, new WorldContext() {

                @Override
                public void resize(int width, int height) {
                }

                @Override
                public boolean isGenerating() {
                    return false;
                }

                @Override
                public void begin() {
                }

                @Override
                public void end() {
                }

                @Override
                public void onReadBuilding() {
                    //read team colors
                    if (current.team != null) {
                        int c = current.team.color.rgba8888();
                        int size = current.block().size;
                        int offsetx = -(size - 1) / 2;
                        int offsety = -(size - 1) / 2;
                        for (int dx = 0; dx < size; dx++) {
                            for (int dy = 0; dy < size; dy++) {
                                int drawx = current.x + dx + offsetx, drawy = current.y + dy + offsety;
                                walls.set(drawx, floors.height - 1 - drawy, c);
                            }
                        }

                        if (current.block() instanceof CoreBlock) {
                            map.teams.add(current.team.id);
                        }
                    }
                }

                @Override
                public Tile tile(int index) {
                    current.x = (short) (index % map.width);
                    current.y = (short) (index / map.width);
                    return current;
                }

                @Override
                public Tile create(int x, int y, int floorID, int overlayID, int wallID) {
                    Block floor = version.getByID(ContentType.block, floorID);
                    Block overlay = version.getByID(ContentType.block, floorID);

                    if (overlayID != 0) {
                        floors.set(x, floors.height - 1 - y, colorFor(Blocks.air, Blocks.air, overlay, Team.derelict));
                    } else {
                        floors.set(x, floors.height - 1 - y, colorFor(Blocks.air, floor, Blocks.air, Team.derelict));
                    }
                    return null;
                }
            }));

            floors.draw(walls, true);
            walls.dispose();
            return floors;
        }
    }

    static class ContainerTile extends Tile {

        public Team team;

        public ContainerTile() {
            super(0, 0);
        }

        @Override
        public void setBlock(Block type) {
            block = type;
        }

        @Override
        protected void preChanged() {
        }

        @Override
        protected void changeBuild(Team team, Prov<Building> entityprov, int rotation) {
        }

        @Override
        protected void changed() {
        }

        @Override
        protected void fireChanged() {
        }

        @Override
        protected void firePreChanged() {
        }
    }

    static class FixedLegacySave extends FixedSave {

        public FixedLegacySave(int version) {
            super(version);
        }

        @Override
        public void readMap(DataInput stream, WorldContext context) throws IOException {
            int width = stream.readUnsignedShort();
            int height = stream.readUnsignedShort();

            //read floor and create tiles first
            for (int i = 0; i < width * height; i++) {
                int x = i % width, y = i / width;
                short floorid = stream.readShort();
                short oreid = stream.readShort();
                int consecutives = stream.readUnsignedByte();
                if (block(floorid) == Blocks.air) floorid = Blocks.stone.id;

                context.create(x, y, floorid, oreid, (short) 0);

                for (int j = i + 1; j < i + 1 + consecutives; j++) {
                    int newx = j % width, newy = j / width;
                    context.create(newx, newy, floorid, oreid, (short) 0);
                }

                i += consecutives;
            }

            //read blocks
            for (int i = 0; i < width * height; i++) {
                Block block = block(stream.readShort());
                ContainerTile tile = (ContainerTile) context.tile(i);
                if (block == null) block = Blocks.air;

                //occupied by multiblock part
                boolean occupied = tile.build != null && !tile.isCenter() && (tile.build.block == block || block == Blocks.air);

                //do not override occupied cells
                if (!occupied) {
                    tile.setBlock(block);
                }

                if (block.hasBuilding()) {
                    try {
                        readChunk(stream, true, in -> {
                            in.readByte(); // version
                            stream.readUnsignedShort(); // health
                            byte packedrot = stream.readByte();
                            boolean readTeam = Pack.leftByte(packedrot) == 8;
                            byte team = readTeam ? stream.readByte() : Pack.leftByte(packedrot);

                            tile.team = Team.get(team);
                            in.skipBytes(lastRegionLength - 1 - 2 - 1 - Mathf.num(readTeam));
                        });
                    } catch (Throwable e) {
                        throw new IOException("Failed to read tile entity of block: " + block, e);
                    }

                    context.onReadBuilding();
                } else {
                    int consecutives = stream.readUnsignedByte();

                    //air is a waste of time and may mess up multiblocks
                    if (block != Blocks.air) {
                        for (int j = i + 1; j < i + 1 + consecutives; j++) {
                            context.tile(j).setBlock(block);
                        }
                    }

                    i += consecutives;
                }
            }
        }
    }

    static class FixedSave extends SaveVersion {

        private MappableContent[][] temporaryMapper;

        public FixedSave(int version) {
            super(version);
        }

        @SuppressWarnings("unchecked")
        private <T extends Content> T getByID(ContentType type, int id) {

            if (temporaryMapper != null && temporaryMapper[type.ordinal()] != null && temporaryMapper[type.ordinal()].length != 0) {
                //-1 = invalid content
                if (id < 0) {
                    return null;
                }
                if (temporaryMapper[type.ordinal()].length <= id || temporaryMapper[type.ordinal()][id] == null) {
                    return (T) content.getBy(type).get(0); //default value is always ID 0
                }
                return (T) temporaryMapper[type.ordinal()][id];
            }

            if (id >= content.getBy(type).size || id < 0) {
                return null;
            }
            return (T) content.getBy(type).get(id);
        }

        protected Block block(int id) {
            return getByID(ContentType.block, id);
        }

        @Override
        public void readMap(DataInput stream, WorldContext context) throws IOException {
            int width = stream.readUnsignedShort();
            int height = stream.readUnsignedShort();

            //read floor and create tiles first
            for (int i = 0; i < width * height; i++) {
                int x = i % width, y = i / width;
                short floorid = stream.readShort();
                short oreid = stream.readShort();
                int consecutives = stream.readUnsignedByte();
                if (block(floorid) == Blocks.air) floorid = Blocks.stone.id;

                context.create(x, y, floorid, oreid, (short) 0);

                for (int j = i + 1; j < i + 1 + consecutives; j++) {
                    int newx = j % width, newy = j / width;
                    context.create(newx, newy, floorid, oreid, (short) 0);
                }

                i += consecutives;
            }

            //read blocks
            for (int i = 0; i < width * height; i++) {
                Block block = block(stream.readShort());
                ContainerTile tile = (ContainerTile) context.tile(i);
                if (block == null) block = Blocks.air;
                boolean isCenter = true;
                byte packedCheck = stream.readByte();
                boolean hadEntity = (packedCheck & 1) != 0;
                boolean hadData = (packedCheck & 2) != 0;

                if (hadEntity) {
                    isCenter = stream.readBoolean();
                }

                //set block only if this is the center; otherwise, it's handled elsewhere
                if (isCenter) {
                    tile.setBlock(block);
                }

                if (hadEntity) {
                    if (isCenter) {
                        if (block.hasBuilding()) {
                            try {
                                readChunk(stream, true, in -> {
                                    in.readByte(); // revision
                                    // ниже код взят с Building.readBase()
                                    in.readFloat(); // health
                                    in.readByte(); // rot
                                    tile.team = Team.get(in.readByte());
                                    in.skipBytes(lastRegionLength - 1 - 4 - 1 - 1);
                                });
                            } catch (Throwable e) {
                                throw new IOException("Failed to read tile entity of block: " + block, e);
                            }
                        } else {
                            skipChunk(stream, true);
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
        }

        @Override
        public void readContentHeader(DataInput stream) throws IOException {
            byte mapped = stream.readByte();

            MappableContent[][] map = new MappableContent[ContentType.all.length][0];

            for (int i = 0; i < mapped; i++) {
                ContentType type = ContentType.all[stream.readByte()];
                short total = stream.readShort();
                map[type.ordinal()] = new MappableContent[total];

                for (int j = 0; j < total; j++) {
                    String name = stream.readUTF();
                    //fallback only for blocks
                    map[type.ordinal()][j] = content.getByName(type, type == ContentType.block ? fallback.get(name, name) : name);
                }
            }

            temporaryMapper = map;
        }
    }
}
