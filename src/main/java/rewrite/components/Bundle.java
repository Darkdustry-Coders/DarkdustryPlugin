package rewrite.components;

import arc.files.Fi;
import arc.struct.ObjectMap;
import arc.util.Log;
import arc.util.Structs;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static pandorum.util.Utils.getPluginResource;
import static rewrite.PluginVars.defaultLanguage;

public class Bundle {

    public static final Locale defaultLocale = new Locale(defaultLanguage);

    private static final ObjectMap<Locale, ResourceBundle> properties = new ObjectMap<>();
    private static final ObjectMap<Locale, MessageFormat> formats = new ObjectMap<>();
    public static Locale[] supportedLocales;

    public static void load() {
        Fi[] bundles = getPluginResource("bundles").list();

        // TODO загружать все ключи и значения прямо при запуске сервера, а не в методе getOrLoad()
        // TODO вернуть локаль router
        supportedLocales = new Locale[bundles.length + 1];
        supportedLocales[supportedLocales.length - 1] = new Locale("router");

        for (int i = 0; i < bundles.length; i++) {
            String code = bundles[i].nameWithoutExtension();
            String[] codes;
            if (!code.contains("_")) { // bundle.properties
                supportedLocales[i] = Locale.ROOT;
            } else if ((codes = code.split("_")).length == 3) { // bundle_uk_UA.properties
                supportedLocales[i] = new Locale(codes[1], codes[2]);
            } else { // bundle_ru.properties
                supportedLocales[i] = new Locale(codes[1]);
            }
        }

        Log.info("[Darkdustry] Загружено локалей: @.", supportedLocales.length);
    }

    public static String get(String key, String defaultValue, Locale locale) {
        try {
            ResourceBundle bundle = getOrLoad(locale);
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return defaultValue;
        }
    }

    public static String get(String key, Locale locale) {
        return get(key, key, locale);
    }

    public static String get(String key) {
        return get(key, defaultLocale);
    }

    public static String format(String key, Locale locale, Object... values) {
        String pattern = get(key, locale);
        if (values.length == 0) {
            return pattern;
        }

        MessageFormat format = formats.get(locale);
        if (!Structs.contains(supportedLocales, locale)) {
            format = formats.get(defaultLocale, () -> new MessageFormat(pattern, defaultLocale));
            format.applyPattern(pattern);
        } else if (format == null) {
            formats.put(locale, format = new MessageFormat(pattern, locale));
        } else {
            format.applyPattern(pattern);
        }
        return format.format(values);
    }

    public static String format(String key, Object... values) {
        return format(key, defaultLocale, values);
    }

    public static ResourceBundle getOrLoad(Locale locale) {
        ResourceBundle bundle = properties.get(locale);
        if (bundle == null) {
            if (Structs.contains(supportedLocales, locale)) {
                properties.put(locale, bundle = ResourceBundle.getBundle("bundles.bundle", locale));
            } else {
                bundle = getOrLoad(defaultLocale);
            }
        }
        return bundle;
    }
}