package darkdustry.database.models;

import arc.struct.Seq;
import arc.util.Nullable;
import darkdustry.database.Database;
import darkdustry.config.Config;
import dev.morphia.annotations.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity(value = "config", useDiscriminator = false)
@NoArgsConstructor
public class ServerConfig {
    @Id
    public String namespace = "global";

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
        isp = isp.replaceAll("\\W", "");

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
            configNs = Database.fetchConfig(Config.config.mode.toString());
        return configNs;
    }

    public static void invalidate() {
        config = Database.fetchConfig();
        configNs = Database.fetchConfig(Config.config.mode.toString());
    }

    public void save() {
        Database.writeConfig(config);
    }

    public interface OptionIn {
        String key();

        String description();

        Seq<OptionIn> inPlace();

        String get();

        @Nullable
        String set(String value, String namespace);
    }

    @AllArgsConstructor
    public static class OptionBool implements OptionIn {
        final String key;
        final String description;
        final String property;

        @Override
        public String key() {
            return key;
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public Seq<OptionIn> inPlace() {
            return Seq.with();
        }

        @Override
        public String get() {
            try {
                var field = ServerConfig.class.getField(property);
                return Boolean.toString(field.getBoolean(ServerConfig.get())
                        || field.getBoolean(ServerConfig.getLocal()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public @Nullable String set(String value, String namespace) {
            boolean val = false;
            if (value.equalsIgnoreCase("t") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("y")
                    || value.equalsIgnoreCase("yes")) {
                val = true;
            } else if (!(value.equalsIgnoreCase("f") || value.equalsIgnoreCase("false") || value.equalsIgnoreCase("n")
                    || value.equalsIgnoreCase("no"))) {
                return "Not a valid boolean value. Use one of: `t/true/y/yes / f/false/n/no`";
            }
            try {
                var field = ServerConfig.class.getField(property);
                var config = ServerConfig.get(namespace);
                field.setBoolean(config, val);
                config.save();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }

    @AllArgsConstructor
    public static class OptionList implements OptionIn {
        final String key;
        final String description;
        final String property;

        @Override
        public String key() {
            return key;
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public Seq<OptionIn> inPlace() {
            return Seq.with(new OptionListAdd(key, property), new OptionListRemove(key, property));
        }

        @Override
        public String get() {
            return "";
        }

        @Override
        public String set(String value, String namespace) {
            return "Please use '" + key + "-add' and '" + key + "-remove to change this list";
        }

        @AllArgsConstructor
        public static class OptionListAdd implements OptionIn {
            final String key;
            final String property;

            @Override
            public String key() {
                return key + "-add";
            }

            @Override
            public String description() {
                return "Append a value";
            }

            @Override
            public Seq<OptionIn> inPlace() {
                return Seq.with();
            }

            @Override
            public String get() {
                return "";
            }

            @Override
            @Nullable
            public String set(String value, String namespace) {
                try {
                    var field = ServerConfig.class.getField(property);
                    var config = ServerConfig.get(namespace);
                    var values = new Seq<>(((String)field.get(config)).toLowerCase().split(";"));
                    var add = new Seq<>(value.toLowerCase().split(";"));
                    add.each(values::addUnique);
                    field.set(config, String.join(";", add));
                    config.save();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                return null;
            }
        }

        @AllArgsConstructor
        public static class OptionListRemove implements OptionIn {
            final String key;
            final String property;

            @Override
            public String key() {
                return key + "-remove";
            }

            @Override
            public String description() {
                return "Remove a value";
            }

            @Override
            public Seq<OptionIn> inPlace() {
                return Seq.with();
            }

            @Override
            public String get() {
                return "";
            }

            @Override
            @Nullable
            public String set(String value, String namespace) {
                try {
                    var field = ServerConfig.class.getField(property);
                    var config = ServerConfig.get(namespace);
                    var values = new Seq<>(((String)field.get(config)).toLowerCase().split(";"));
                    var add = new Seq<>(value.split(";"));
                    values.removeAll(add);
                    field.set(config, String.join(";", add));
                    config.save();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                return null;
            }
        }
    }

    public static Seq<OptionIn> options() {
        var options = new Seq<OptionIn>();

        options.add(new OptionBool("graylist-enabled", "Require some users to attach a Discord account.",
                "graylistEnabled"));
        options.add(new OptionBool("graylist-mobile", "Graylist mobile internet users.",
                "graylistMobile"));
        options.add(new OptionBool("graylist-hosting", "Graylist connection from hosting providers.",
                "graylistHosting"));
        options.add(new OptionBool("graylist-proxy", "Graylist proxies.",
                "graylistProxy"));

        options.add(new OptionList("graylist-ips", "Graylist IPs and subnets",
                "graylistIPs"));
        options.add(new OptionList("graylist-isps", "Graylist ISPs",
                "graylistISPs"));

        return options;
    }
}
