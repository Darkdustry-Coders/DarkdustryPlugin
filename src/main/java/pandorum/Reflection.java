package pandorum;

import arc.struct.Seq;
import arc.util.Log;
import mindustry.gen.Player;
import pandorum.annotations.commands.ClientCommand;
import pandorum.annotations.commands.ServerCommand;
import pandorum.annotations.commands.gamemodes.*;
import pandorum.annotations.commands.gamemodes.containers.DisabledGamemodes;
import pandorum.annotations.commands.gamemodes.containers.RequiredGamemodes;
import pandorum.annotations.events.EventListener;
import pandorum.comp.Config;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

public class Reflection {
    public static Seq<Class<?>> getClasses(String basePackage) {
        String packageName = basePackage.replace('.', '/');
        URL pckg = PandorumPlugin.class.getClassLoader().getResource(packageName);

        if (pckg == null)
            throw new NullPointerException("Cannot get resources to package " + basePackage);

        String pckgStr = pckg.toString();

        try {
            ZipFile zipFile = new ZipFile(pckgStr.substring("jar:file:".length(), pckgStr.lastIndexOf("!/")));
            return Seq.with(zipFile.stream()
                    .filter(e -> e.getName().startsWith(packageName)
                            && e.getName().endsWith(".class"))
                    .<Class<?>>map(e -> {
                        try{
                            String name = e.getName().replace('/', '.');
                            name = name.substring(0, name.lastIndexOf('.'));

                            return Class.forName(name);
                        }catch(ClassNotFoundException ex){
                            Log.err(ex.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toArray(Class<?>[]::new)
            );
        } catch (IOException e) {
            Log.err(e.getMessage());
            return new Seq<Class<?>>();
        }
    }

    public static Seq<Method> getAnnotatedMethods(String basePackage, Class<? extends Annotation> annotationClass) {
        return getClasses(basePackage).map(classObject ->
                Arrays.stream(classObject.getMethods()).filter(method -> method.isAnnotationPresent(annotationClass))
        ).reduce(
                new Seq<Method>(),
                (Stream<Method> initial, Seq<Method> elem) -> elem.addAll(initial.toArray(Method[]::new))
        );
    }

    public static Seq<Method> getCommandsMethods(String basePackage, Class<? extends Annotation> annotation, Class<?>[] allowedParams) {
        return getAnnotatedMethods(basePackage, annotation).filter(
                method -> Modifier.isStatic(method.getModifiers()) && method.getReturnType().equals(Void.TYPE) && Arrays.equals(method.getParameterTypes(), allowedParams)
        );
    }

    public static Seq<Method> getClientCommands(String basePackage, Config.Gamemode gamemode) {
        return getCommandsMethods(basePackage, ClientCommand.class, new Class<?>[] { String[].class, Player.class })
                .map(method -> {
                    Seq<Config.Gamemode> disabledGamemodes = new Seq<>();
                    Seq<Config.Gamemode> reqiuredGamemodes = new Seq<>();

                    boolean requireSimpleGamemode = method.isAnnotationPresent(RequireSimpleGamemode.class);
                    boolean hasRequiredGamemodes = method.isAnnotationPresent(DisabledGamemodes.class);
                    boolean hasDisabledGamemodes = method.isAnnotationPresent(RequiredGamemodes.class);
                    boolean requirePvP = method.isAnnotationPresent(RequirePvP.class);
                    boolean disablePvp = method.isAnnotationPresent(DisablePvP.class);

                    if (hasRequiredGamemodes) {
                        reqiuredGamemodes = Seq.with(
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

                    if(!(
                            (hasRequiredGamemodes && hasDisabledGamemodes)
                         || (hasRequiredGamemodes && !reqiuredGamemodes.contains(gamemode))
                         || (hasDisabledGamemodes && disabledGamemodes.contains(gamemode))
                         || (hasRequiredGamemodes && requireSimpleGamemode)
                         || (requireSimpleGamemode && !gamemode.isSimple)
                         || (requirePvP && disablePvp)
                         || (requirePvP && !gamemode.isPvP)
                         || (disablePvp && gamemode.isPvP)
                    )) return method;
                    return null;
                })
                .filter(Objects::nonNull);
    }

    public static Seq<Method> getServerCommands(String basePackage) {
        return getCommandsMethods(basePackage, ServerCommand.class, new Class<?>[] { String[].class });
    }

    public static Seq<Method> getEventsMethods(String basePackage) {
        return getAnnotatedMethods(basePackage, EventListener.class).filter(
                method ->  Modifier.isStatic(method.getModifiers()) && method.getReturnType().equals(Void.TYPE) && Arrays.equals(method.getParameterTypes(), new Class<?>[] { method.getAnnotation(EventListener.class).eventType() })
        );
    }
}
