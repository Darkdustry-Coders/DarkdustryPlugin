package pandorum.comp.admin;

public enum ResponseType {
    ACCEPT(1),
    SKIP(2),
    BAN(3);

    private final int value;


    ResponseType(int value) {
        this.value = value;
    }


    public static ResponseType getByValue(int value) {
        for (ResponseType status : values()) {
            if (status.value == value) return status;
        }

        throw new IllegalArgumentException("Invalid code: " + value);
    }
}
