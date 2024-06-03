package darkdustry.utils;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.serialization.Json;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.net.URI;

@NoArgsConstructor
public class IpTables {
    private static final Json json = new Json() {{
        setUsePrototypes(false);
    }};
    private static final Seq<String> hotspots = new Seq<>();

    static {
        try (var stream = IpTables.class.getClassLoader().getResourceAsStream("hotspots.txt")) {
            assert stream != null;
            String[] hotspots = new String(stream.readAllBytes()).split("\n");
            for (int i = 0; i < hotspots.length; i++) {
                hotspots[i] = hotspots[i].trim();
            }
            IpTables.hotspots.add(hotspots);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String isp;
    public boolean mobile;
    public boolean proxy;
    public boolean hosting;

    public boolean isHotspot() {
        return mobile || hotspots.contains(isp);
    }

    private static final ObjectMap<String, IpTables> cache = new ObjectMap<>();

    public static @Nullable IpTables of(String address) {
        var entry = cache.get(address);
        if (entry != null) return entry;

        try {
            var connection = URI.create("http://ip-api.com/json/" + address + "?fields=16974336").toURL().openConnection();
            connection.setConnectTimeout(500);
            connection.setReadTimeout(500);
            connection.connect();
            var stream = connection.getInputStream();
            entry = json.fromJson(IpTables.class, stream);
            cache.put(address, entry);
            return entry;
        } catch (Exception e) {
            Log.warn("Couldn't fetch ip metadata: ", e);
        }

        return null;
    }
}
