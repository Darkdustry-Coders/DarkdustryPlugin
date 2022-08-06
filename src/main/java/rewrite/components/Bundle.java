package rewrite.components;

import arc.files.Fi;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import rewrite.DarkdustryPlugin;
import rewrite.utils.Find;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static rewrite.PluginVars.*;
import static rewrite.utils.Utils.*;

public class Bundle { // TODO: переделать bundle.properties с нуля, ибо команды сильно поменялись

    public static final Locale defaultLocale = new Locale(defaultLanguage);
    public static final Seq<Locale> supportedLocales = new Seq<>();

    private static final ObjectMap<Locale, ResourceBundle> bundles = new ObjectMap<>();
    private static final ObjectMap<Locale, MessageFormat> formats = new ObjectMap<>();

    public static void load() {
        Seq<Fi> files = getPluginResource("bundles").seq();

        files.each(file -> {
            String[] codes = file.nameWithoutExtension().split("_");

            if (codes.length == 1) { // bundle.properties
                supportedLocales.add(Locale.ROOT);
            } else if (codes.length == 2) { // bundle_ru.properties
                supportedLocales.add(new Locale(codes[1]));
            } else if (codes.length == 3) { // bundle_uk_UA.properties
                supportedLocales.add(new Locale(codes[1], codes[2]));
            }
        });

        supportedLocales.each(locale -> {
            bundles.put(locale, ResourceBundle.getBundle("bundles.bundle", locale));
            formats.put(locale, new MessageFormat("", locale));
        });

        DarkdustryPlugin.info("Загружено @ локалей, локаль по умолчанию: @.", supportedLocales.size, defaultLocale.toLanguageTag());
    }

    public static String get(String key, String defaultValue, Locale locale) {
        try {
            ResourceBundle bundle = bundles.get(locale, bundles.get(defaultLocale));
            return bundle.getString(key);
        } catch (MissingResourceException ignored) {
            return defaultValue;
        }
    }

    public static String get(String key, Locale locale) {
        return get(key, key, locale);
    }

    public static String get(String key, String defaultValue) {
        return get(key, defaultValue, defaultLocale);
    }

    public static String get(String key) {
        return get(key, defaultLocale);
    }

    public static String format(String key, Locale locale, Object... values) {
        String pattern = get(key, locale);
        if (values.length == 0) {
            return pattern;
        }

        MessageFormat format = formats.get(locale, formats.get(defaultLocale));
        format.applyPattern(pattern);
        return format.format(values);
    }

    public static String format(String key, Object... values) {
        return format(key, defaultLocale, values);
    }

    public static void bundled(Player player, String key, Object... values) {
        player.sendMessage(format(key, Find.locale(player.locale), values));
    }

    public static void sendToChat(String key, Object... values) {
        Groups.player.each(player -> bundled(player, key, values));
    }
}