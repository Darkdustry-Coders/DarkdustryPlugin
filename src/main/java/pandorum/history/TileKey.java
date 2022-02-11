package pandorum.history;

public class TileKey {
    public final int x;
    public final int y;
    public int serialNumber;

    public TileKey(int x, int y, int serialNumber) {
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