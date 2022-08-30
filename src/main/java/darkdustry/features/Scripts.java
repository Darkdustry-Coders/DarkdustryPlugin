package darkdustry.features;

import darkdustry.DarkdustryPlugin;

import static mindustry.Vars.*;

public class Scripts {

    public static void load() {
        var scripts = dataDirectory.child("scripts").seq();

        if (scripts.size > 0) {
            DarkdustryPlugin.info("Found @ startup scripts.", scripts.size);

            scripts.each(fi -> {
                try {
                    mods.getScripts().runConsole(fi.readString());
                } catch (Exception e) {
                    DarkdustryPlugin.error("Error loading startup script @: @", fi.name(), e);
                }
            });

            DarkdustryPlugin.info("Loaded @ startup scripts.", scripts.size);
        }
    }
}
