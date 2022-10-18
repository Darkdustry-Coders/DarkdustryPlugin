package darkdustry.features;

import arc.util.Time;
import darkdustry.DarkdustryPlugin;

import static mindustry.Vars.*;

public class Scripts {

    public static void load() {
        dataDirectory.child("scripts").walk(fi -> {
            if (!fi.extEquals("js")) return;

            try {
                Time.mark();
                mods.getScripts().runConsole(fi.readString());
                DarkdustryPlugin.info("Loaded script @ in @ ms.", fi.name(), Time.elapsed());
            } catch (Exception e) {
                DarkdustryPlugin.error("Error loading script @: @", fi.name(), e);
            }
        });
    }
}