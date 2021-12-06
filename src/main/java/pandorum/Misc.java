package pandorum;

import arc.files.Fi;
import arc.struct.Seq;
import arc.util.CommandHandler;
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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;
import java.util.zip.ZipFile;

import static mindustry.Vars.*;

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

    public static void handleClientCommands(CommandHandler handler, String basePackage, Config.Gamemode gamemode) {
        Class<?>[] requiredParams = new Class<?>[] { String[].class, Player.class };

        getAnnotatedMethods(basePackage, ClientCommand.class).each(
                method -> Modifier.isStatic(method.getModifiers()) && Arrays.equals(method.getParameterTypes(), requiredParams),
                method -> {
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

                    String skipMessage = "Skip command:" + method.getDeclaringClass().getPackageName() + "." + method.getDeclaringClass() + "." + method.getName();

                    if (hasRequiredGamemodes && hasDisabledGamemodes) {
                        Log.info("Disable and require gamemodes. " + skipMessage);
                        return;
                    }

                    if (hasRequiredGamemodes && !reqiuredGamemodes.contains(gamemode)) {
                        Log.info("Required gamemode is not match current gamemode" + "(" + gamemode.name() + ")" + "." + skipMessage);
                        return;
                    }

                    if (hasDisabledGamemodes && disabledGamemodes.contains(gamemode)) {
                        Log.info("Current gamemode" + "(" + gamemode.name() + ")" + " is disabled. " + skipMessage);
                        return;
                    }

                    if (hasRequiredGamemodes && requireSimpleGamemode) {
                        Log.info("Require gamemode and require simple gamemode. " + skipMessage);
                        return;
                    }

                    if (requireSimpleGamemode && !gamemode.isSimple) {
                        Log.info("Require simple gamemode and current gamemode" + "(" + gamemode.name() + ")" + " is not simple. " + skipMessage);
                        return;
                    }

                    if (requirePvP && disablePvp) {
                        Log.info("Require and disable PvP. " + skipMessage);
                        return;
                    }
                    if (requirePvP && !gamemode.isPvP) {
                        Log.info("Require PvP and current gamemode" + "(" + gamemode.name() + ")" + " is not PvP. " + skipMessage);
                        return;
                    }

                    if (disablePvp && gamemode.isPvP) {
                        Log.info("Disable PvP and current gamemode"+ "(" + gamemode.name() + ")" +" is PvP. " + skipMessage);
                        return;
                    }

                    ClientCommand commandAnnotaion = method.getAnnotation(ClientCommand.class);

                    handler.register(
                                    commandAnnotaion.name(),
                                    commandAnnotaion.args(),
                                    commandAnnotaion.description(),
                                    (String[] args, Player player) -> {
                                        if (commandAnnotaion.admin() && !player.admin) return;
                                        try {
                                            method.invoke(null, player, args);
                                        } catch (Exception e) {}
                                    }
                            );
                });
    }

    public static void handleServerCommands(CommandHandler handler, String basePackage) {
        Class<?>[] requiredParams = new Class<?>[] { String[].class };

        getAnnotatedMethods(basePackage, ServerCommand.class).each(
                method -> Modifier.isStatic(method.getModifiers()) && Arrays.equals(method.getParameterTypes(), requiredParams),
                method -> {
                    ServerCommand commandAnnotation = method.getAnnotation(ServerCommand.class);
                    handler.register(
                                    commandAnnotation.name(),
                                    commandAnnotation.args(),
                                    commandAnnotation.description(),
                                    (String[] args) -> {
                                        try {
                                            method.invoke(null, (Object) args);
                                        } catch (Exception e) {}
                                    }
                    );
                }
        );
    }
}
