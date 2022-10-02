package darkdustry.components;

import arc.struct.*;
import darkdustry.DarkdustryPlugin;
import darkdustry.utils.Find;
import mindustry.gen.*;

import java.text.MessageFormat;
import java.util.*;

import static darkdustry.PluginVars.*;
import static darkdustry.utils.Utils.getPluginResource;

public class Bundle {

    public static final Locale defaultLocale = new Locale(defaultLanguage);
    public static final Seq<Locale> supportedLocales = new Seq<>();

    private static final ObjectMap<Locale, ResourceBundle> bundles = new ObjectMap<>();

    public static void load() {
        var files = getPluginResource("bundles").seq().filter(fi -> fi.extEquals("properties"));

        files.each(fi -> {
            var codes = fi.nameWithoutExtension().split("_");

            if (codes.length == 2) { // bundle_ru.properties
                supportedLocales.add(new Locale(codes[1]));
            } else if (codes.length == 3) { // bundle_uk_UA.properties
                supportedLocales.add(new Locale(codes[1], codes[2]));
            }
        });

        supportedLocales.each(locale -> bundles.put(locale, ResourceBundle.getBundle("bundles.bundle", locale)));

        DarkdustryPlugin.info("Loaded @ locales, default locale is @.", supportedLocales.size, defaultLocale.toLanguageTag());
    }

    public static String get(String key, String defaultValue, Locale locale) {
        try {
            var bundle = bundles.get(locale, bundles.get(defaultLocale));
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return defaultValue;
        }
    }

    public static String get(String key, Locale locale) {
        return get(key, key, locale);
    }

    public static String get(String key, String defaultValue) {
        return get(key, defaultValue, defaultLocale);
    }

    public static String format(String key, Locale locale, Object... values) {
        var pattern = get(key, locale);
        if (values.length == 0) return pattern;

        return MessageFormat.format(pattern, values);
    }

    public static void bundled(Player player, String key, Object... values) {
        player.sendMessage(format(key, Find.locale(player.locale), values));
    }

    public static void bundled(Player player, Player from, String text, String key, Object... values) {
        player.sendMessage(format(key, Find.locale(player.locale), values), from, text);
    }

    public static void sendToChat(String key, Object... values) {
        Groups.player.each(player -> bundled(player, key, values));
    }
}