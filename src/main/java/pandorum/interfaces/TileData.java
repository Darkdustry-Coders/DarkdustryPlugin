package pandorum.interfaces;

import arc.math.geom.Vec2;

public class TileData {
    public Vec2 location;
    public int queryPosition;

    public TileData(Vec2 location, int queryPosition) {
        this.location = location;
        this.queryPosition = queryPosition;
    }
}
