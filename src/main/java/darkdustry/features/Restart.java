package darkdustry.features;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static darkdustry.config.Config.config;

public class Restart {

    private Restart() {}

    public static boolean restart = false;
    public static boolean copyPlugin = false;

    public static void copyPlugin() throws IOException {
        Files.copy(
                Path.of(config.pluginSource),
                Path.of(".").resolve("config/plugins/DarkdustryPlugin.jar"),
                StandardCopyOption.REPLACE_EXISTING);
    }
}
