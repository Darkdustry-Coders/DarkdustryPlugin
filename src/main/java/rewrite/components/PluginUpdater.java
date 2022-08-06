package rewrite.components;

import arc.util.Http;
import mindustry.mod.Mods.LoadedMod;

import static rewrite.utils.Utils.*;

public class PluginUpdater {

    // TODO сделать чета примерно такое
    public static void checkUpdate() {
        LoadedMod plugin = getPlugin();
        String version = plugin.meta.version;

        Http.get(plugin.meta.repo).submit(response -> {
            if (response.getHeader("version") == version) return;
        });
    }
}
