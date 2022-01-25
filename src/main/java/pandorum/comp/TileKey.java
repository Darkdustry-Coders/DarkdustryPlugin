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
}