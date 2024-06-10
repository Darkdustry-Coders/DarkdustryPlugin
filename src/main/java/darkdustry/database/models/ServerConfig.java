package darkdustry.database.models;

import arc.util.Nullable;
import darkdustry.database.Database;
import dev.morphia.annotations.*;
import lombok.NoArgsConstructor;

@Entity(value = "config", useDiscriminator = false)
@NoArgsConstructor
public class ServerConfig {
    @Id
    public String namespace = "global"; // A feature I just thought of that I'll never implement.

    public boolean graylistEnabled = false;
    public boolean graylistMobile = false;
    public boolean graylistHosting = false;
    public boolean graylistProxy = false;

    @Nullable private static ServerConfig config;

    public static ServerConfig get() {
        if (config == null) config = Database.fetchConfig();
        return config;
    }

    public static void invalidate() {
        config = Database.fetchConfig();
    }

    public void save() {
        Database.writeConfig(config);
    }
}
