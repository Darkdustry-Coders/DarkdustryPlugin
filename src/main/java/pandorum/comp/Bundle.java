package pandorum.comp;

import arc.files.Fi;
import arc.struct.ObjectMap;
import arc.struct.StringMap;
import arc.util.Strings;
import arc.util.Structs;
import mindustry.gen.Iconc;
import pandorum.PandorumPlugin;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import static mindustry.Vars.mods;
import static pandorum.PluginVars.defaultLocale;

public class Bundle {

    public static final Locale[] supportedLocales;
    private static final ObjectMap<Locale, StringMap> bundles = new ObjectMap<>();
    private static final ObjectMap<Locale, MessageFormat> formats = new ObjectMap<>();

    static {
        Fi[] files = mods.list().find(mod -> mod.main instanceof PandorumPlugin).root.child("bundles").list();
        supportedLocales = new Locale[files.length + 1];
        supportedLocales[supportedLocales.length - 1] = new Locale("router");

        for (int i = 0; i < files.length; i++) {
            String code = files[i].nameWithoutExtension();
            code = code.substring("bundle_".length());
            if (code.contains("_")) {
                String[] codes = code.split("_");
                supportedLocales[i] = new Locale(codes[0], codes[1]);
            } else {
                supportedLocales[i] = new Locale(code);
            }
        }
    }

    public static Locale defaultLocale() {
        return Structs.find(supportedLocales, locale -> locale.toString().equals(defaultLocale));
    }

    public static String get(String key, Locale locale) {
        return get(key, key, locale);
    }

    public static String get(String key, String defaultValue, Locale locale) {
        StringMap bundle = getOrLoad(locale);
        return bundle != null ? bundle.get(key, defaultValue) : defaultValue;
    }

    public static String format(String key, Locale locale, Object... values) {
        String pattern = get(key, locale);
        MessageFormat format = formats.get(locale);
        if (!Structs.contains(supportedLocales, locale)) {
            format = formats.get(defaultLocale(), () -> new MessageFormat(pattern, defaultLocale()));
            format.applyPattern(pattern);
        } else if (format == null) {
            format = new MessageFormat(pattern, locale);
            formats.put(locale, format);
        } else {
            format.applyPattern(pattern);
        }
        return format.format(values);
    }

    private static StringMap getOrLoad(Locale locale) {
        StringMap bundle = bundles.get(locale);
        if (bundle == null && locale.getDisplayName().equals("router")) {
            StringMap router = new StringMap();
            getOrLoad(defaultLocale()).each((k, v) -> router.put(k, Strings.stripColors(v).replaceAll("\\S", String.valueOf(Iconc.blockRouter))));
            bundles.put(locale, bundle = router);
        } else if (bundle == null && Structs.contains(supportedLocales, locale)) {
            bundles.put(locale, bundle = load(locale));
        }
        return bundle != null ? bundle : bundles.get(defaultLocale());
    }

    private static StringMap load(Locale locale) {
        StringMap properties = new StringMap();
        ResourceBundle bundle = ResourceBundle.getBundle("bundles.bundle", locale);
        for (String s : bundle.keySet()) {
            properties.put(s, bundle.getString(s));
        }
        return properties;
    }
}
