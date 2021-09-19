package pandorum.comp;

import arc.graphics.Color;
import mindustry.gen.Player;

public class RainbowPlayerEntry {
    public Player player;
    public int hue;
    public String stripedName;

    public static void changeEntryColor(RainbowPlayerEntry entry) {
        int hue = entry.hue;
        if (hue < 360) hue++;
        else hue = 0;

        String color = "[#" + Color.HSVtoRGB(hue, 100f, 100f) + "]";
        entry.player.name = color + entry.stripedName;
        entry.hue = hue;
    }
}
