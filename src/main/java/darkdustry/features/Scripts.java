package darkdustry.features;

import arc.util.Time;
import darkdustry.DarkdustryPlugin;

import static mindustry.Vars.dataDirectory;
import static mindustry.Vars.mods;

public class Scripts {

    public static void load() {
        var scripts = dataDirectory.child("scripts").seq().filter(fi -> fi.extEquals("js"));

        if (scripts.isEmpty()) return;

        DarkdustryPlugin.info("Found @ startup scripts.", scripts.size);
        Time.mark();

        scripts.each(fi -> {
            try {
                mods.getScripts().runConsole(fi.readString());
            } catch (Exception e) {
                DarkdustryPlugin.error("Error loading startup script @: @", fi.name(), e);
            }
        });

        DarkdustryPlugin.info("Loaded @ startup scripts in @ ms.", scripts.size, Time.elapsed());
    }
}
