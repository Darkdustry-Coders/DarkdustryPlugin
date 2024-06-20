package darkdustry.database.models;

import arc.util.Nullable;
import darkdustry.database.Database;
import darkdustry.config.Config;
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
    public String graylistISPs = "";
    public String graylistIPs = "";

    @Nullable
    private static ServerConfig config;
    @Nullable
    private static ServerConfig configNs;

    public static boolean graylistEnabled() {
        return get().graylistEnabled || getLocal().graylistEnabled;
    }

    public static boolean graylistMobile() {
        return get().graylistMobile || getLocal().graylistMobile;
    }

    public static boolean graylistHosting() {
        return get().graylistHosting || getLocal().graylistMobile;
    }

    public static boolean graylistProxy() {
        return get().graylistProxy || getLocal().graylistProxy;
    }

    public static boolean ispGraylisted(String isp) {
        isp = isp.replaceAll("[^\\w]", "");

        for (String x : get().graylistISPs.split(";")) {
            if (x.equalsIgnoreCase(isp))
                return true;
        }
        for (String x : getLocal().graylistISPs.split(";")) {
            if (x.equalsIgnoreCase(isp))
                return true;
        }

        return false;
    }

    public static boolean ipGraylisted(String ip) {
        for (String x : get().graylistIPs.split(";")) {
            if (ip.toLowerCase().startsWith(x.toLowerCase()))
                return true;
        }
        for (String x : getLocal().graylistIPs.split(";")) {
            if (ip.toLowerCase().startsWith(x.toLowerCase()))
                return true;
        }

        return false;
    }

    public static ServerConfig get() {
        if (config == null)
            config = Database.fetchConfig();
        return config;
    }

    public static ServerConfig get(String namespace) {
        if (namespace.equals("global"))
            return get();
        if (namespace.equals(Config.config.mode.toString()))
            return getLocal();
        return Database.fetchConfig(namespace);
    }

    public static ServerConfig getLocal() {
        if (configNs == null)
            config = Database.fetchConfig(Config.config.mode.toString());
        return config;
    }

    public static void invalidate() {
        config = Database.fetchConfig();
    }

    public void save() {
        Database.writeConfig(config);
    }
}
