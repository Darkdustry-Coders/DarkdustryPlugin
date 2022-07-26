package pandorum.components;

import arc.files.Fi;
import arc.struct.ObjectMap;
import arc.struct.StringMap;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Structs;
import mindustry.gen.Iconc;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import static pandorum.PluginVars.defaultLanguage;
import static pandorum.util.Utils.getPluginResource;

public class Bundle {

    // TODO стилизовать все тексты в бандлах, пофиксить грамматические ошибки, сделать одинаковые цвета и так далее

    public static final ObjectMap<Locale, StringMap> bundles = new ObjectMap<>();
    public static final ObjectMap<Locale, MessageFormat> formats = new ObjectMap<>();
    public static final Locale defaultLocale = new Locale(defaultLanguage);
    public static Locale[] supportedLocales;

    public static void load() {
        Fi[] files = getPluginResource("bundles").list();
        supportedLocales = new Locale[files.length + 1];
        supportedLocales[supportedLocales.length - 1] = new Locale("router");

        for (int i = 0; i < files.length; i++) {
            String code = files[i].nameWithoutExtension();
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

    public static String get(String key) {
        return get(key, defaultLocale);
    }

    public static String get(String key, Locale locale) {
        StringMap bundle = getOrLoad(locale);
        return bundle != null ? bundle.get(key, key) : key;
    }

    public static String format(String key, Locale locale, Object... values) {
        String pattern = get(key, locale);
        if (values.length == 0) return pattern;

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

    private static StringMap getOrLoad(Locale locale) {
        StringMap bundle = bundles.get(locale);
        if (bundle == null && Structs.contains(supportedLocales, locale)) {
            bundles.put(locale, bundle = load(locale));
        }
        return bundle != null ? bundle : bundles.get(defaultLocale);
    }

    private static StringMap load(Locale locale) {
        StringMap properties = new StringMap();
        ResourceBundle bundle = ResourceBundle.getBundle("bundles.bundle", locale);
        if (locale.getDisplayName().equals("router")) {
            getOrLoad(defaultLocale).each((key, value) -> properties.put(key, Strings.stripColors(value).replaceAll("\\S", String.valueOf(Iconc.blockRouter))));
        } else {
            bundle.keySet().forEach(key -> properties.put(key, bundle.getString(key)));
        }
        return properties;
    }
}
