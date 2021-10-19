package pandorum.ranks;

public enum RankType {
    player(1),
    active(2),
    veteran(3);

    public final int number;

    RankType(int number) {
        this.number = number;
    }

    public static RankType getByNumber(int number) {
        return switch(number) {
            case 3 -> veteran;
            case 2 -> active;
            default -> player;
        };
    }
}
