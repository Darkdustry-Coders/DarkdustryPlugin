package pandorum.comp;

import arc.struct.ObjectMap;
import arc.struct.StringMap;
import arc.util.Log;
import mindustry.gen.Iconc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Icons {
    private static ObjectMap<String, Character> icons;

    public static void init() {
        icons = new ObjectMap<>();
        List<String> prefixes = List.of("item", "liquid", "unit", "team", "status" );

        Arrays.stream(Iconc.class.getFields()).forEach(field ->  {
            if (field.getType().equals(char.class) && Modifier.isStatic(field.getModifiers())) {
                try {
                    char value = field.getChar(null);
                    String name = field.getName();
                    if (prefixes.stream().anyMatch(name::startsWith))
                        icons.put(name, value);
                } catch (Exception e) { Log.err(e.getMessage()); }
            }
        });

        Log.info("Loaded icons:".concat(icons.keys().toSeq().reduce("", (name, textAll) -> textAll.concat(" ").concat(name))));
    }

    public static char get(String key) {
        return icons.containsKey(key) ? icons.get(key) : ' ';
    }
}