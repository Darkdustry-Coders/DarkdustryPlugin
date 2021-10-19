package pandorum.ranks;

import mindustry.gen.Iconc;

public enum RankType {
    player(1, Iconc.units),
    active(2, Iconc.statusElectrified),
    veteran(3, Iconc.statusOverclock),
    moderator(4, Iconc.hammer);

    public final int permission;
    public final char sign;

    RankType(int permission, char sign) {
        this.permission = permission;
        this.sign = sign;
    }

    public static RankType getByPermission(int permission) {
        return switch(permission) {
            case 2 -> active;
            case 3 -> veteran;
            case 4 -> moderator;
            default -> player;
        };
    }
}
