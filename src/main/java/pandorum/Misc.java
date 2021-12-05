package pandorum;

import arc.files.Fi;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Structs;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.maps.Map;
import pandorum.annotations.commands.ClientCommand;
import pandorum.annotations.commands.ServerCommand;
import pandorum.annotations.containers.DisabledGamemodes;
import pandorum.annotations.containers.RequiredGamemodes;
import pandorum.annotations.gamemodes.*;
import pandorum.comp.Bundle;
import pandorum.comp.Config;
import pandorum.comp.Icons;
import pandorum.struct.CommandType;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;
import java.util.zip.ZipFile;

import static mindustry.Vars.maps;
import static mindustry.Vars.saveExtension;
import static mindustry.Vars.saveDirectory;

public abstract class Misc {

    private Misc() {}

    public static String colorizedTeam(Team team) {
        return Strings.format("[white]@[#@]@", Icons.get(team.name), team.color, team.name);
    }

    public static Map findMap(String name) {
        Seq<Map> mapsList = maps.customMaps();
        for (int i = 0; i < mapsList.size; i++) {
            Map map = mapsList.get(i);
            if ((Strings.canParsePositiveInt(name) && i == Strings.parseInt(name) - 1) || Strings.stripColors(map.name()).equalsIgnoreCase(name) || Strings.stripColors(map.name()).contains(name)) {
                return map;
            }
        }
        return null;
    }

    public static Fi findSave(String name) {
        Seq<Fi> savesList = Seq.with(saveDirectory.list()).filter(f -> Objects.equals(f.extension(), saveExtension));
        for (int i = 0; i < savesList.size; i++) {
            Fi save = savesList.get(i);
            if ((Strings.canParsePositiveInt(name) && i == Strings.parseInt(name) - 1) || save.nameWithoutExtension().equalsIgnoreCase(name) || save.nameWithoutExtension().contains(name)) {
                return save;
            }
        }
        return null;
    }

    public static Player findByName(String name) {
        return Groups.player.find(p -> Strings.stripColors(p.name).equalsIgnoreCase(Strings.stripColors(name)) || Strings.stripColors(p.name).contains(Strings.stripColors(name)));
    }

    public static Locale findLocale(String lang) {
        Locale locale = Structs.find(Bundle.supportedLocales, l -> l.toString().equals(lang) || lang.startsWith(l.toString()));
        return locale != null ? locale : Bundle.defaultLocale();
    }

    public static boolean adminCheck(Player player) {
        if (!player.admin()) {
            bundled(player, "commands.permission-denied");
            return true;
        }
        return false;
    }

    public static void bundled(Player player, String key, Object... values) {
        player.sendMessage(Bundle.format(key, findLocale(player.locale), values));
    }

    public static void sendToChat(String key, Object... values) {
        Groups.player.each(p -> bundled(p, key, values));
    }

    public static String formatTime(Date time) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Moscow")));
        return format.format(time);
    }

    public static Seq<Class> loadClasses(String basePackage) {
        Seq<Class> classes = new Seq<>();
        ClassLoader classLoader = PandorumPlugin.class.getClassLoader();
        String packageName = basePackage.replace('.', '/');
        URL pckg = classLoader.getResource(packageName);
        if (pckg == null)
            throw new NullPointerException("Cannot get resources to package " + basePackage);
        String pckgStr = pckg.toString();
        String path = pckgStr.substring("jar:file:".length(), pckgStr.lastIndexOf("!/"));

        try {
            ZipFile zipFile = new ZipFile(path);
            zipFile.stream()
                    .filter(e -> e.getName().startsWith(packageName)
                            && e.getName().endsWith(".class"))
                    .forEach(e -> {
                        try{
                            String name = e.getName().replace('/', '.');
                            name = name.substring(0, name.lastIndexOf('.'));

                            classes.add(Class.forName(name));
                        }catch(ClassNotFoundException ex){
                            ex.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

    public static Seq<Method> getAnnotatedMethods(String basePackage, Class<? extends Annotation> annotationClass) {
        Seq<Method> methods = new Seq<>();
        loadClasses(basePackage).each(classObject ->
                Arrays.stream(classObject.getMethods()).forEach(method -> {
                            if (method.isAnnotationPresent(annotationClass))
                                methods.add(method);
                        }
                )
        );
        return methods;
    }

    public static Seq<Method> getCommandMethods(String basePackage, CommandType type, Config.Gamemode gamemode) {
        Seq<Method> methods = new Seq<>();

        Class<?>[] requiredParams = switch (type) {
            case Client -> new Class<?>[] { Player.class, String[].class };
            case Server -> new Class<?>[] { String[].class };
        };

        Class<? extends Annotation> annotation = switch (type) {
            case Server -> ServerCommand.class;
            case Client -> ClientCommand.class;
        };

        getAnnotatedMethods(basePackage, annotation).each(method -> {
            if (
                    Modifier.isStatic(method.getModifiers())
                 && Arrays.equals(method.getParameterTypes(), requiredParams)
            ) {
                Seq<Config.Gamemode> disabledGamemodes = new Seq<>();
                Seq<Config.Gamemode> reqiuredGamemodes = new Seq<>();

                boolean requireSimpleGamemode = method.isAnnotationPresent(RequireSimpleGamemode.class);
                boolean hasRequiredGamemodes = method.isAnnotationPresent(DisabledGamemodes.class);
                boolean hasDisabledGamemodes = method.isAnnotationPresent(RequiredGamemodes.class);
                boolean requirePvP = method.isAnnotationPresent(RequirePvP.class);
                boolean disablePvp = method.isAnnotationPresent(DisablePvP.class);

                if (hasRequiredGamemodes) {
                    disabledGamemodes = Seq.with(
                            Arrays.stream(method.getAnnotation(DisabledGamemodes.class).value())
                                    .map(DisableGamemode::Gamemode).toArray(Config.Gamemode[]::new)
                    );
                }
                if (hasDisabledGamemodes) {
                    disabledGamemodes = Seq.with(
                            Arrays.stream(method.getAnnotation(RequiredGamemodes.class).value())
                                    .map(RequireGamemode::Gamemode).toArray(Config.Gamemode[]::new)
                    );
                }

                if (hasRequiredGamemodes && hasDisabledGamemodes) {
                    Log.info("Cannot disable and ban one gamemode");
                    return;
                }
                if (hasRequiredGamemodes && !reqiuredGamemodes.contains(gamemode)) {
                    Log.info("Not initialized command(command not reqiure this gamemode)");
                    return;
                }
                if (hasDisabledGamemodes && disabledGamemodes.contains(gamemode)) {
                    Log.info("Not initialized command(gamemode is disabled for this command");
                    return;
                }
                
                if (hasRequiredGamemodes && requireSimpleGamemode) {
                    Log.info("Cannot require gamemode and reqire simple gamemode");
                    return;
                }
                
                if (requireSimpleGamemode && !gamemode.isSimple) {
                    Log.info("Not simple gamemode. Skip command");
                    return;
                }

                if (requirePvP && disablePvp) {
                    Log.info("Require and diasble pvp");
                    return;
                }

                if (requirePvP && !gamemode.isPvP) {
                    Log.info("Not pvp. Skip");
                    return;
                }

                if (disablePvp && gamemode.isPvP) {
                    Log.info("Gamemode is pvp. Skip");
                    return;
                }

                methods.add(method);
            } else
                Log.info("Annotated method " + method.getName() + " is not static or it has invalid parameters");
        });

        return methods;
    }
}
