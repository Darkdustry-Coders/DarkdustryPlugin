package pandorum.comp;

public class TileKey {
    public short x;
    public short y;
    public byte serialNumber;

    public TileKey(short x, short y, byte serialNumber) {
        this.x = x;
        this.y = y;
        this.serialNumber = serialNumber;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TileKey tileKey &&
            tileKey.x == this.x &&
            tileKey.y == this.y &&
            tileKey.serialNumber == this.serialNumber;
    }
}